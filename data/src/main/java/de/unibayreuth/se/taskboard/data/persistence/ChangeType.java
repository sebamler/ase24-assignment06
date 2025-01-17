package de.unibayreuth.se.taskboard.data.persistence;

/**
 * Available event types for event sourcing.
 */
public enum ChangeType {
    INSERT,
    UPDATE,
    DELETE
}
