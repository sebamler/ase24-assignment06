package de.unibayreuth.se.taskboard.business.impl;

import de.unibayreuth.se.taskboard.business.exceptions.DuplicateNameException;
import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.business.exceptions.MalformedRequestException;
import de.unibayreuth.se.taskboard.business.ports.UserPersistenceService;
import de.unibayreuth.se.taskboard.business.ports.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserPersistenceService userPersistenceService;

    @Override
    public void clear() {
        userPersistenceService.clear();
    }

    @Override
    @NonNull
    public List<User> getAll() {
        return userPersistenceService.getAll();
    }

    @Override
    @NonNull
    public Optional<User> getById(@NonNull UUID id) {
        return userPersistenceService.getById(id);
    }

    @Override
    @NonNull
    public User create(@NonNull User user) throws MalformedRequestException, DuplicateNameException {
        if (user.getId() != null) {
            throw new MalformedRequestException("User ID must not be set.");
        }
        return userPersistenceService.upsert(user);
    }
}
