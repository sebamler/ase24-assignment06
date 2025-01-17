package de.unibayreuth.se.taskboard.business.exceptions;

public class MalformedRequestException extends RuntimeException {
    public MalformedRequestException(String message) {
        super(message);
    }
}
