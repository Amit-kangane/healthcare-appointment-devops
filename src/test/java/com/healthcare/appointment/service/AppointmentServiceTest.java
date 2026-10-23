package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.AppointmentRequest;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.exception.AppointmentNotFoundException;
import com.healthcare.appointment.exception.InvalidAppointmentException;
import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.AppointmentStatus;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private AppointmentMetricsService metricsService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment sampleAppointment;
    private AppointmentRequest validRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(3);
        sampleAppointment = new Appointment(
                1L,
                "Fictional Patient Test",
                "Dr. Test Surgeon",
                futureTime,
                AppointmentStatus.SCHEDULED,
                "Routine checkup",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        validRequest = new AppointmentRequest(
                "Fictional Patient Test",
                "Dr. Test Surgeon",
                futureTime,
                AppointmentStatus.SCHEDULED,
                "Routine checkup"
        );
    }

    @Test
    @DisplayName("Should return all appointments successfully")
    void getAllAppointments_Success() {
        when(repository.findAll()).thenReturn(List.of(sampleAppointment));

        List<AppointmentResponse> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientName()).isEqualTo("Fictional Patient Test");
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should return appointment by ID successfully")
    void getAppointmentById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleAppointment));

        AppointmentResponse result = appointmentService.getAppointmentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPatientName()).isEqualTo("Fictional Patient Test");
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when ID does not exist")
    void getAppointmentById_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(999L))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create appointment successfully and increment counter")
    void createAppointment_Success() {
        when(repository.save(any(Appointment.class))).thenReturn(sampleAppointment);

        AppointmentResponse response = appointmentService.createAppointment(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(metricsService).incrementCreated();
        verify(repository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should reject appointment creation when appointment date is in past")
    void createAppointment_PastDate_ThrowsException() {
        validRequest.setAppointmentDateTime(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("must be in the future");

        verify(repository, never()).save(any());
        verify(metricsService, never()).incrementCreated();
    }

    @Test
    @DisplayName("Should update existing appointment successfully")
    void updateAppointment_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleAppointment));
        when(repository.save(any(Appointment.class))).thenReturn(sampleAppointment);

        AppointmentResponse response = appointmentService.updateAppointment(1L, validRequest);

        assertThat(response).isNotNull();
        verify(metricsService).incrementUpdated();
        verify(repository).save(sampleAppointment);
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when updating non-existent appointment")
    void updateAppointment_NotFound() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.updateAppointment(404L, validRequest))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete appointment successfully")
    void deleteAppointment_Success() {
        when(repository.existsById(1L)).thenReturn(true);

        appointmentService.deleteAppointment(1L);

        verify(repository).deleteById(1L);
        verify(metricsService).incrementCancelled();
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when deleting non-existent appointment")
    void deleteAppointment_NotFound() {
        when(repository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(404L))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
