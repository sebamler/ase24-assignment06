package de.unibayreuth.se.taskboard.business.domain;

import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Domain class that stores the task metadata.
 */
@Data
public class Task implements Serializable, Identifiable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Nullable
        private UUID id; // null when task is not persisted yet
        @NonNull
        private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("UTC")); // set on task creation
        @NonNull
        private LocalDateTime updatedAt = createdAt; // set on task updates
        @NonNull
        private String title;
        @NonNull
        private String description;
        @NonNull
        private TaskStatus status = TaskStatus.TODO;
        @Nullable
        private UUID assigneeId; // null when no user is assigned yet

        @Override
        public long getSerialVersionUID() {
                return serialVersionUID;
        }
}
