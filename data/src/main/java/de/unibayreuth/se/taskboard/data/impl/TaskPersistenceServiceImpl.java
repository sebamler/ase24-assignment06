package de.unibayreuth.se.taskboard.data.impl;

import de.unibayreuth.se.taskboard.business.domain.Task;
import de.unibayreuth.se.taskboard.business.exceptions.TaskNotFoundException;
import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import de.unibayreuth.se.taskboard.business.ports.TaskPersistenceService;
import de.unibayreuth.se.taskboard.data.mapper.TaskEntityMapper;
import de.unibayreuth.se.taskboard.data.persistence.TaskEntity;
import de.unibayreuth.se.taskboard.data.persistence.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-based implementation of the task persistence service that the business layer provides as a port.
 */
@Service
@RequiredArgsConstructor
class TaskPersistenceServiceImpl implements TaskPersistenceService {
    private final TaskRepository repository;
    private final TaskEntityMapper mapper;

    @Override
    public void clear() {
        repository.deleteAllInBatch();
        repository.flush();
    }

    @Override
    @NonNull
    public List<Task> getAll() {
        return repository.findAll().stream()
                .map(mapper::fromEntity)
                .toList();
    }

    @Override
    @NonNull
    public Optional<Task> getById(@NonNull UUID id) throws TaskNotFoundException {
        return repository.findById(id)
                .map(mapper::fromEntity);
    }

    @Override
    @NonNull
    public List<Task> getByStatus(@NonNull TaskStatus status) {
        return repository.findByStatus(status).stream()
                .map(mapper::fromEntity)
                .toList();
    }

    @Override
    @NonNull
    public List<Task> getByAssignee(@NonNull UUID userId) {
        return repository.findByAssigneeId(userId).stream()
                .map(mapper::fromEntity)
                .toList();
    }

    @Override
    @NonNull
    public Task upsert(@NonNull Task task) throws TaskNotFoundException {
        if (task.getId() == null) {
            // create new task
            return mapper.fromEntity(repository.saveAndFlush(mapper.toEntity(task)));
        }

        // update existing task
        TaskEntity taskEntity = repository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + task.getId() + " does not exist."));
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setUpdatedAt(task.getUpdatedAt());
        taskEntity.setStatus(task.getStatus());
        taskEntity.setAssigneeId(task.getAssigneeId());
        return mapper.fromEntity(repository.saveAndFlush(taskEntity));
    }

    @Override
    public void delete(@NonNull UUID id) throws TaskNotFoundException {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException("Task with ID " + id + " does not exist.");
        }
        repository.deleteById(id);
    }
}
