package de.unibayreuth.se.taskboard.api.dtos;

import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for task metadata.
 *
 */
@Data
public class TaskDto {
        @Nullable
        private final UUID id; // task id is null when creating or update a new task
        @Nullable
        private final LocalDateTime createdAt; // is null when using DTO to create or update a new task
        @Nullable
        private final LocalDateTime updatedAt; // is null when using DTO to create or update a new task
        @NotNull
        @NotBlank
        @Size(max = 255, message = "Title can be at most 255 characters long.")
        private final String title;
        @NotNull
        private final String description;
        @Nullable
        private final TaskStatus status; // can be null on creation and is then set to default status
        @Nullable
        @Valid
        private final UserDto assignee; // null when no user is assigned yet
}
