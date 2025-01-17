package de.unibayreuth.se.taskboard.data.impl;

import de.unibayreuth.se.taskboard.business.domain.*;
import de.unibayreuth.se.taskboard.business.exceptions.DuplicateNameException;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import de.unibayreuth.se.taskboard.business.ports.UserPersistenceService;
import de.unibayreuth.se.taskboard.data.mapper.UserEntityMapper;
import de.unibayreuth.se.taskboard.data.persistence.UserEntity;
import de.unibayreuth.se.taskboard.data.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-based implementation of the user persistence service that the business layer provides as a port.
 */
@Service
@RequiredArgsConstructor
public class UserPersistenceServiceImpl implements UserPersistenceService {
    private final UserRepository repository;
    private final UserEntityMapper mapper;

    @Override
    public void clear() {
        repository.deleteAllInBatch();
        repository.flush();
    }

    @Override
    @NonNull
    public List<User> getAll() {
        return repository.findAll().stream()
                .map(mapper::fromEntity)
                .toList();
    }

    @Override
    @NonNull
    public Optional<User> getById(UUID id) {
        return repository.findById(id)
                .map(mapper::fromEntity);
    }

    @Override
    @NonNull
    public User upsert(User user) throws UserNotFoundException, DuplicateNameException {
        if (user.getId() == null) {
            // create new user
            if (repository.existsByName(user.getName())) {
                throw new DuplicateNameException("User with name " + user.getName() + " already exists.");
            }
            return mapper.fromEntity(repository.saveAndFlush(mapper.toEntity(user)));
        }

        // update existing user
        UserEntity userEntity = repository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + user.getId() + " does not exist."));
        userEntity.setName(user.getName());
        return mapper.fromEntity(repository.saveAndFlush(userEntity));
    }
}
