package com.healthcare.appointment.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Service dedicated to custom business metrics exported to Prometheus via Micrometer.
 */
@Service
public class AppointmentMetricsService {

    private final Counter appointmentsCreatedCounter;
    private final Counter appointmentsCancelledCounter;
    private final Counter appointmentsUpdatedCounter;

    public AppointmentMetricsService(MeterRegistry registry) {
        this.appointmentsCreatedCounter = Counter.builder("healthcare_appointments_created_total")
                .description("Total number of appointments successfully created")
                .tag("module", "appointments")
                .register(registry);

        this.appointmentsCancelledCounter = Counter.builder("healthcare_appointments_cancelled_total")
                .description("Total number of appointments cancelled")
                .tag("module", "appointments")
                .register(registry);

        this.appointmentsUpdatedCounter = Counter.builder("healthcare_appointments_updated_total")
                .description("Total number of appointments updated")
                .tag("module", "appointments")
                .register(registry);
    }

    public void incrementCreated() {
        appointmentsCreatedCounter.increment();
    }

    public void incrementCancelled() {
        appointmentsCancelledCounter.increment();
    }

    public void incrementUpdated() {
        appointmentsUpdatedCounter.increment();
    }
}
