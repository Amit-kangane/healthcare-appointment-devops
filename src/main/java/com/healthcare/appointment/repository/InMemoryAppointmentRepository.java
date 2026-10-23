package com.healthcare.appointment.repository;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.AppointmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe In-Memory implementation of AppointmentRepository.
 *
 * NOTE FOR COLLEGE DEVOPS DEMONSTRATION:
 * This repository keeps data in RAM using a ConcurrentHashMap.
 * Data is fast, self-contained, and requires no external database.
 * NOTE: All data is ephemeral and will be lost whenever the application
 * or container restarts.
 */
@Repository
public class InMemoryAppointmentRepository implements AppointmentRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryAppointmentRepository.class);

    private final Map<Long, Appointment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public InMemoryAppointmentRepository() {
        seedInitialFictionalData();
    }

    /**
     * Seeds initial fictional appointment records for demonstration purposes.
     * All names and data are completely fictional.
     */
    private void seedInitialFictionalData() {
        log.info("================================================================================");
        log.info("NOTICE: Initializing InMemoryAppointmentRepository with FICTIONAL patient data.");
        log.info("Storage is in-memory only; data resets upon application or container restart.");
        log.info("================================================================================");

        LocalDateTime now = LocalDateTime.now();

        save(new Appointment(
                null,
                "Fictional Patient Alice Smith",
                "Dr. Robert Vance (Cardiology)",
                now.plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                AppointmentStatus.SCHEDULED,
                "Routine annual cardiology consultation",
                now,
                now
        ));

        save(new Appointment(
                null,
                "Fictional Patient Bob Jones",
                "Dr. Sarah Patel (Pediatrics)",
                now.plusDays(4).withHour(14).withMinute(30).withSecond(0).withNano(0),
                AppointmentStatus.CONFIRMED,
                "General health checkup and wellness consultation",
                now,
                now
        ));

        save(new Appointment(
                null,
                "Fictional Patient Charlie Green",
                "Dr. Michael Chang (Orthopedics)",
                now.plusDays(7).withHour(11).withMinute(15).withSecond(0).withNano(0),
                AppointmentStatus.SCHEDULED,
                "Follow-up consultation for knee physiotherapy",
                now,
                now
        ));
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>(storage.values());
        list.sort(Comparator.comparing(Appointment::getId));
        return list;
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Appointment save(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();
        if (appointment.getId() == null) {
            appointment.setId(idGenerator.getAndIncrement());
            appointment.setCreatedAt(now);
            appointment.setUpdatedAt(now);
        } else {
            appointment.setUpdatedAt(now);
        }

        storage.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.containsKey(id);
    }

    @Override
    public long count() {
        return storage.size();
    }

    /**
     * Resets storage for unit testing.
     */
    public void reset() {
        storage.clear();
        idGenerator.set(1);
    }
}
