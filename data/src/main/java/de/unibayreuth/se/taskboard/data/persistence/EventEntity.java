package de.unibayreuth.se.taskboard.data.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import de.unibayreuth.se.taskboard.business.domain.Identifiable;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Database entity for an event.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
@Builder(toBuilder = true)
public class EventEntity {
    /**
     * This is the unique event ID represented as UUID.
     */
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    /**
     * Type of the event.
     */
    @Enumerated(EnumType.STRING)
    private ChangeType type;

    /**
     * Entity related to the event.
     */
    private String entity;

    /**
     * Version of the entity.
     */
    @Column(name = "entity_version")
    private Long entityVersion;

    /**
     * User who triggered the event.
     */
    @Column(name = "created_by")
    private UUID createdBy; // nullable as we are currently do not have authentication and hence do not know the user

    /**
     * Timestamp when the event was triggered (should not be set manually but by the database on insert).
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * This is the payload of the event stored as binary json.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> body;

    public static EventEntity insertEventOf(Identifiable entity,
                                            UUID userId,
                                            ObjectMapper objectMapper) {
        return EventEntity.builder()
                .type(ChangeType.INSERT)
                .entity(entity.getClass().getSimpleName()) // to keep it readable, we're not using the FQN
                .entityVersion(entity.getSerialVersionUID())
                .createdBy(userId)
                .body(objectMapper.convertValue(entity, new TypeReference<>() { })) // convert to Map<String, Object>
                .build();
    }

    public static EventEntity deleteEventOf(Identifiable entity,
                                            UUID userId) {
        return EventEntity.builder()
                .type(ChangeType.DELETE)
                .entity(entity.getClass().getSimpleName())
                .entityVersion(entity.getSerialVersionUID())
                .createdBy(userId)
                .body(Map.of("id", String.valueOf(entity.getId())))
                .build();
    }

    public static EventEntity updateEventOf(Identifiable entity,
                                            UUID userId,
                                            ObjectMapper objectMapper) {
        return EventEntity.builder()
                .type(ChangeType.UPDATE)
                .entity(entity.getClass().getSimpleName()) // to keep it readable, we're not using the FQN
                .entityVersion(entity.getSerialVersionUID())
                .createdBy(userId)
                .body(objectMapper.convertValue(entity, new TypeReference<>() { })) // convert to Map<String, Object>
                .build();
    }
}

