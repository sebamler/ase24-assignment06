package de.unibayreuth.se.taskboard.business.ports;

import de.unibayreuth.se.taskboard.business.exceptions.DuplicateNameException;
import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for the implementation of the user data service that the business layer provides as a port.
 */
public interface UserPersistenceService {
    void clear();
    @NonNull
    List<User> getAll();
    @NonNull
    Optional<User> getById(UUID id);
    @NonNull
    User upsert(User user) throws UserNotFoundException, DuplicateNameException;
}
