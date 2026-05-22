package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    /** Admin: all appointments */
    @GetMapping
    public ResponseEntity<?> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }

    /** Single appointment by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Patient: their appointments */
    @GetMapping("/patient")
    public ResponseEntity<?> getByPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
    }

    /** Doctor: their appointments */
    @GetMapping("/doctor")
    public ResponseEntity<?> getByDoctor(@RequestParam Long doctorId) {
        return ResponseEntity.ok(appointmentRepository.findByDoctorId(doctorId));
    }

    /** Doctor marks appointment as COMPLETED */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> markComplete(@PathVariable Long id) {
        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) return ResponseEntity.notFound().build();
        if (apt.getStatus() != AppointmentStatus.BOOKED) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only BOOKED appointments can be marked complete."));
        }
        apt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(apt);
        return ResponseEntity.ok(Map.of("message", "Appointment marked as completed."));
    }

    /** Doctor uploads prescription (PDF or image) */
    @PutMapping("/{id}/prescription")
    public ResponseEntity<?> uploadPrescription(@PathVariable Long id,
                                                @RequestParam("file") MultipartFile file) {
        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) return ResponseEntity.notFound().build();
        try {
            apt.setPrescriptionFile(file.getBytes());
            appointmentRepository.save(apt);
            return ResponseEntity.ok(Map.of("message", "Prescription uploaded successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /** Patient / Doctor downloads prescription as base64 */
    @GetMapping("/{id}/prescription")
    public ResponseEntity<?> getPrescription(@PathVariable Long id) {
        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) return ResponseEntity.notFound().build();
        if (apt.getPrescriptionFile() == null) {
            return ResponseEntity.status(404).body(Map.of("error", "No prescription uploaded yet."));
        }
        String b64 = java.util.Base64.getEncoder().encodeToString(apt.getPrescriptionFile());
        return ResponseEntity.ok(Map.of("prescription", b64));
    }
}
