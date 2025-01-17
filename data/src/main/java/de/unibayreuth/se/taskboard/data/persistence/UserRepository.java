package de.unibayreuth.se.taskboard.data.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for persisting users.
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByName(String name);
}
