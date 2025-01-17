package de.unibayreuth.se.taskboard.api.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for users.
 *
 */
@Data
public class UserDto {
        @Nullable
        private final UUID id; // null when using DTO to create a new user
        @Nullable
        private final LocalDateTime createdAt; // null when using DTO to create a new user
        @NotNull
        @Pattern(regexp = "\\w+")
        private final String name;
}
