package de.unibayreuth.se.taskboard.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unibayreuth.se.taskboard.business.domain.Task;
import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import de.unibayreuth.se.taskboard.business.exceptions.TaskNotFoundException;
import de.unibayreuth.se.taskboard.business.ports.TaskPersistenceService;
import de.unibayreuth.se.taskboard.data.mapper.TaskEntityMapper;
import de.unibayreuth.se.taskboard.data.persistence.EventEntity;
import de.unibayreuth.se.taskboard.data.persistence.EventRepository;
import de.unibayreuth.se.taskboard.data.persistence.TaskEntity;
import de.unibayreuth.se.taskboard.data.persistence.TaskRepository;
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

/**
 * Event-sourcing-based implementation of the task persistence service that the business layer provides as a port.
 */

@Service
@RequiredArgsConstructor
@Primary
public class TaskPersistenceServiceEventSourcingImpl implements TaskPersistenceService {
    private final TaskRepository taskRepository;
    private final TaskEntityMapper taskEntityMapper;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void clear() {
        taskRepository.findAll()
                .forEach(taskEntity -> eventRepository.saveAndFlush(
                        EventEntity.deleteEventOf(taskEntityMapper.fromEntity(taskEntity), null))
                );
        if (taskRepository.count() != 0) {
            throw new IllegalStateException("Tasks not successfully deleted.");
        }
    }

    @NonNull
    @Override
    public List<Task> getAll() {
        return taskRepository.findAll().stream()
                .map(taskEntityMapper::fromEntity)
                .toList();
    }

    @NonNull
    @Override
    public Optional<Task> getById(@NonNull UUID id) {
        return taskRepository.findById(id)
                .map(taskEntityMapper::fromEntity);
    }

    @NonNull
    @Override
    public List<Task> getByStatus(@NonNull TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(taskEntityMapper::fromEntity)
                .toList();
    }

    @NonNull
    @Override
    public List<Task> getByAssignee(@NonNull UUID userId) {
        return taskRepository.findByAssigneeId(userId).stream()
                .map(taskEntityMapper::fromEntity)
                .toList();
    }

    @NonNull
    @Override
    @Transactional
    public Task upsert(@NonNull Task task) throws TaskNotFoundException {
        if (task.getId() == null) {
            // Create a new task
            task.setId(UUID.randomUUID());
            task.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
            task.setUpdatedAt(task.getCreatedAt());
            TaskEntity taskEntity = taskEntityMapper.toEntity(task);
            TaskEntity savedEntity = taskRepository.saveAndFlush(taskEntity);

            // Log the INSERT event
            eventRepository.saveAndFlush(EventEntity.insertEventOf(task, null, objectMapper));
            return taskEntityMapper.fromEntity(savedEntity);
        }

        // Update an existing task
        TaskEntity existingTaskEntity = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + task.getId() + " does not exist."));
        existingTaskEntity.setTitle(task.getTitle());
        existingTaskEntity.setDescription(task.getDescription());
        existingTaskEntity.setStatus(task.getStatus());
        existingTaskEntity.setAssigneeId(task.getAssigneeId());
        existingTaskEntity.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        TaskEntity updatedEntity = taskRepository.saveAndFlush(existingTaskEntity);

        // Log the UPDATE event
        eventRepository.saveAndFlush(EventEntity.updateEventOf(task, null, objectMapper));
        return taskEntityMapper.fromEntity(updatedEntity);
    }

    @Override
    public void delete(@NonNull UUID id) throws TaskNotFoundException {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " does not exist."));

        // Log the DELETE event
        eventRepository.saveAndFlush(EventEntity.deleteEventOf(taskEntityMapper.fromEntity(taskEntity), null));

        // Delete the task
        taskRepository.delete(taskEntity);
        taskRepository.flush();

        if (taskRepository.existsById(id)) {
            throw new IllegalStateException("Task with ID " + id + " was not successfully deleted.");
        }
    }
}
