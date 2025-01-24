package de.unibayreuth.se.taskboard.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.business.exceptions.DuplicateNameException;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import de.unibayreuth.se.taskboard.business.ports.UserPersistenceService;
import de.unibayreuth.se.taskboard.data.mapper.UserEntityMapper;
import de.unibayreuth.se.taskboard.data.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Primary
public class UserPersistenceServiceEventSourcingImpl implements UserPersistenceService {
    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void clear() {
        userRepository.findAll()
                .forEach(userEntity -> eventRepository.saveAndFlush(
                        EventEntity.deleteEventOf(userEntityMapper.fromEntity(userEntity), null))
                );
        if (userRepository.count() != 0) {
            throw new IllegalStateException("Tasks not successfully deleted.");
        }
    }

    @NonNull
    @Override
    public List<User> getAll() {
        return userRepository.findAll().stream()
                .map(userEntityMapper::fromEntity)
                .toList();
    }

    @NonNull
    @Override
    public Optional<User> getById(UUID id) {
        return userRepository.findById(id)
                .map(userEntityMapper::fromEntity);
    }

    @NonNull
    @Override
    @Transactional
    public User upsert(User user) throws UserNotFoundException, DuplicateNameException {
        if (user.getId() == null) {
            // Create a new user
            if (userRepository.existsByName(user.getName())) {
                throw new DuplicateNameException("User with name " + user.getName() + " already exists.");
            }
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
            UserEntity userEntity = userEntityMapper.toEntity(user);
            UserEntity savedEntity = userRepository.saveAndFlush(userEntity);

            // Log the INSERT event
            eventRepository.saveAndFlush(EventEntity.insertEventOf(user, null, objectMapper));
            return userEntityMapper.fromEntity(savedEntity);
        }

        // Update an existing user
        UserEntity existingUserEntity = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + user.getId() + " does not exist."));
        existingUserEntity.setName(user.getName());
        UserEntity updatedEntity = userRepository.saveAndFlush(existingUserEntity);

        // Log the UPDATE event
        eventRepository.saveAndFlush(EventEntity.updateEventOf(user, null, objectMapper));
        return userEntityMapper.fromEntity(updatedEntity);
    }
}
