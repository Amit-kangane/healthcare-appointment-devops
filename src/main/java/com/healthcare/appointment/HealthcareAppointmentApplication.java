package com.healthcare.appointment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for Healthcare Appointment Management System.
 * DevOps Capstone Project.
 */
@SpringBootApplication
public class HealthcareAppointmentApplication {

    private static final Logger log = LoggerFactory.getLogger(HealthcareAppointmentApplication.class);

    public static void main(String[] args) {
        log.info("Starting Healthcare Appointment Management System...");
        SpringApplication.run(HealthcareAppointmentApplication.class, args);
        log.info("Healthcare Appointment Management System is UP and running.");
    }
}
