package de.unibayreuth.se.taskboard.business.impl;

import de.unibayreuth.se.taskboard.business.domain.*;
import de.unibayreuth.se.taskboard.business.exceptions.MalformedRequestException;
import de.unibayreuth.se.taskboard.business.exceptions.TaskNotFoundException;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import de.unibayreuth.se.taskboard.business.ports.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskPersistenceService taskPersistenceService;
    private final UserPersistenceService userPersistenceService;

    @Override
    public void clear() {
        taskPersistenceService.clear();
    }

    @Override
    @NonNull
    public Task create(@NonNull Task task) throws MalformedRequestException, UserNotFoundException {
        if (task.getId() != null) {
            throw new MalformedRequestException("Task ID must not be set.");
        }
        return upsert(task);
    }

    @Override
    @NonNull
    public List<Task> getAll() {
        return taskPersistenceService.getAll();
    }

    @Override
    @NonNull
    public Task getById(@NonNull UUID id) throws TaskNotFoundException {
        return taskPersistenceService.getById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " does not exist."));
    }

    @Override
    @NonNull
    public List<Task> getByStatus(@NonNull TaskStatus status) {
        return taskPersistenceService.getByStatus(status);
    }

    @Override
    @NonNull
    public List<Task> getByAssignee(@NonNull UUID userId) throws UserNotFoundException {
        return taskPersistenceService.getByAssignee(userId);
    }

    @Override
    @NonNull
    public Task upsert(@NonNull Task task) throws TaskNotFoundException, UserNotFoundException {
        if (task.getId() != null) {
            verifyTaskExists(task.getId());
            task.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        }
        if (task.getAssigneeId() != null) {
            verifyUserExists(task.getAssigneeId());
        }
        return taskPersistenceService.upsert(task);
    }

    @Override
    public void delete(@NonNull UUID id) throws TaskNotFoundException {
        taskPersistenceService.delete(id);
    }

    private void verifyTaskExists(@NonNull UUID id) throws TaskNotFoundException {
        taskPersistenceService.getById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " does not exist."));
    }

    private void verifyUserExists(@NonNull UUID id) throws UserNotFoundException {
        userPersistenceService.getById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " does not exist."));
    }
}
