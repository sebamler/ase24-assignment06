package de.unibayreuth.se.taskboard.business.domain;

import java.util.UUID;

public interface Identifiable {
    UUID getId();
    long getSerialVersionUID();
}
