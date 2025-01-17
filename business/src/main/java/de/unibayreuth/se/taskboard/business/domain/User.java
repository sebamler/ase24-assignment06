package de.unibayreuth.se.taskboard.business.domain;

import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;


/**
 * Domain class that represents a user.
 */
@Data
public class User implements Serializable, Identifiable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Nullable
        private UUID id; // null when user is not persisted yet
        @NonNull
        private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("UTC")); // set on task creation
        @NonNull
        private String name;

        @Override
        public long getSerialVersionUID() {
                return serialVersionUID;
        }
}
