package de.unibayreuth.se.taskboard.business.ports;

import de.unibayreuth.se.taskboard.business.domain.Task;
import de.unibayreuth.se.taskboard.business.exceptions.MalformedRequestException;
import de.unibayreuth.se.taskboard.business.exceptions.TaskNotFoundException;
import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the implementation of the list service that the business layer provides as a port.
 */
public interface TaskService {
    void clear();
    @NonNull
    Task create(@NonNull Task task) throws MalformedRequestException, UserNotFoundException;
    @NonNull
    List<Task> getAll();
    @NonNull
    Task getById(@NonNull UUID id) throws TaskNotFoundException;
    @NonNull
    List<Task> getByStatus(@NonNull TaskStatus status);
    @NonNull
    List<Task> getByAssignee(@NonNull UUID userId) throws UserNotFoundException;
    @NonNull
    Task upsert(@NonNull Task task) throws TaskNotFoundException, UserNotFoundException;
    void delete(@NonNull UUID id) throws TaskNotFoundException;
}
