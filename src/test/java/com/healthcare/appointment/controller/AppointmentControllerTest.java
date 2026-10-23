package com.healthcare.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.appointment.dto.AppointmentRequest;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.exception.AppointmentNotFoundException;
import com.healthcare.appointment.model.AppointmentStatus;
import com.healthcare.appointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private AppointmentResponse sampleResponse;
    private AppointmentRequest validRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2).withNano(0);
        sampleResponse = new AppointmentResponse(
                1L,
                "Fictional Patient Test",
                "Dr. John Doe",
                appointmentTime,
                AppointmentStatus.SCHEDULED,
                "Regular dental checkup",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        validRequest = new AppointmentRequest(
                "Fictional Patient Test",
                "Dr. John Doe",
                appointmentTime,
                AppointmentStatus.SCHEDULED,
                "Regular dental checkup"
        );
    }

    @Test
    @DisplayName("GET /api/appointments - Should return list of appointments with HTTP 200")
    void getAllAppointments_ShouldReturnList() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/appointments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].patientName", is("Fictional Patient Test")))
                .andExpect(jsonPath("$[0].doctorName", is("Dr. John Doe")));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} - Should return appointment by ID with HTTP 200")
    void getAppointmentById_Found() throws Exception {
        when(appointmentService.getAppointmentById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/appointments/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.patientName", is("Fictional Patient Test")))
                .andExpect(jsonPath("$.status", is("SCHEDULED")));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} - Should return HTTP 404 when appointment does not exist")
    void getAppointmentById_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(999L))
                .thenThrow(new AppointmentNotFoundException(999L));

        mockMvc.perform(get("/api/appointments/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    @Test
    @DisplayName("POST /api/appointments - Should create appointment and return HTTP 201 with Location header")
    void createAppointment_Success() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/appointments/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.patientName", is("Fictional Patient Test")));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return HTTP 400 when required fields are missing")
    void createAppointment_ValidationFailure_BlankFields() throws Exception {
        AppointmentRequest invalidRequest = new AppointmentRequest();
        invalidRequest.setPatientName(""); // Blank
        invalidRequest.setDoctorName("   "); // Blank
        invalidRequest.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return HTTP 400 when appointment date is in past")
    void createAppointment_ValidationFailure_PastDate() throws Exception {
        AppointmentRequest pastDateRequest = new AppointmentRequest(
                "Jane Doe",
                "Dr. Smith",
                LocalDateTime.now().minusDays(2), // Past date
                AppointmentStatus.SCHEDULED,
                "Checkup"
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastDateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id} - Should update appointment and return HTTP 200")
    void updateAppointment_Success() throws Exception {
        when(appointmentService.updateAppointment(eq(1L), any(AppointmentRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/appointments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.patientName", is("Fictional Patient Test")));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id} - Should return HTTP 404 when updating non-existent appointment")
    void updateAppointment_NotFound() throws Exception {
        when(appointmentService.updateAppointment(eq(404L), any(AppointmentRequest.class)))
                .thenThrow(new AppointmentNotFoundException(404L));

        mockMvc.perform(put("/api/appointments/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /api/appointments/{id} - Should return HTTP 204 No Content upon successful deletion")
    void deleteAppointment_Success() throws Exception {
        doNothing().when(appointmentService).deleteAppointment(1L);

        mockMvc.perform(delete("/api/appointments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/appointments/{id} - Should return HTTP 404 when deleting non-existent appointment")
    void deleteAppointment_NotFound() throws Exception {
        doThrow(new AppointmentNotFoundException(404L)).when(appointmentService).deleteAppointment(404L);

        mockMvc.perform(delete("/api/appointments/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
