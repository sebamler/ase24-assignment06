package de.unibayreuth.se.taskboard.business.ports;

import de.unibayreuth.se.taskboard.business.exceptions.DuplicateNameException;
import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.business.exceptions.MalformedRequestException;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    void clear();
    @NonNull
    List<User> getAll();
    @NonNull
    Optional<User> getById(@NonNull UUID id);
    @NonNull
    User create(@NonNull User user) throws MalformedRequestException, DuplicateNameException;
}
