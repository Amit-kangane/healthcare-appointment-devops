package com.healthcare.appointment.exception;

/**
 * Exception thrown when business validation fails for an appointment.
 */
public class InvalidAppointmentException extends RuntimeException {

    public InvalidAppointmentException(String message) {
        super(message);
    }
}
