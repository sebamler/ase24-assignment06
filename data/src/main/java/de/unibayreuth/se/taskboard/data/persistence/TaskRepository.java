package de.unibayreuth.se.taskboard.data.persistence;

import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for persisting tasks.
 */
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByStatus(TaskStatus status);
    List<TaskEntity> findByAssigneeId(UUID userId);
}
