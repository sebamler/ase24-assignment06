package de.unibayreuth.se.taskboard.business.ports;

import de.unibayreuth.se.taskboard.business.domain.Task;
import de.unibayreuth.se.taskboard.business.exceptions.TaskNotFoundException;
import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for the implementation of the task data service that the business layer provides as a port.
 */
public interface TaskPersistenceService {
    void clear();
    @NonNull
    List<Task> getAll();
    @NonNull
    Optional<Task> getById(@NonNull UUID id);
    @NonNull
    List<Task> getByStatus(@NonNull TaskStatus status);
    @NonNull
    List<Task> getByAssignee(@NonNull UUID userId);
    @NonNull
    Task upsert(@NonNull Task task) throws TaskNotFoundException;
    void delete(@NonNull UUID id) throws TaskNotFoundException;
}
