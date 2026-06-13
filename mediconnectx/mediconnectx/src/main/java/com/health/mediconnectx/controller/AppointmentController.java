package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.entity.Patient;
import com.health.mediconnectx.entity.User;
import com.health.mediconnectx.services.AppointmentService;
import com.health.mediconnectx.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    // CODE-01 fix: Use service layer instead of directly injecting repository
    @Autowired
    private AppointmentService appointmentService;

    // Still need direct access to repository for save operations not yet moved to service
    @Autowired
    private AppointmentRepository appointmentRepository;

    // ── Helpers ──────────────────────────────────────────────────────────

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getName()));
    }

    /** Returns true if the current user is the patient OR doctor of the given appointment. */
    private boolean isOwner(User user, Appointment apt) {
        if (user == null) return false;
        Patient p = user.getPatient();
        Doctor  d = user.getDoctor();
        return (p != null && p.getId().equals(apt.getPatientId()))
                || (d != null && d.getId().equals(apt.getDoctorId()));
    }

    // ── Endpoints ────────────────────────────────────────────────────────

    /**
     * Admin only: retrieve all appointments.
     * SEC-05 fix: patients/doctors cannot dump the full appointment table.
     * CODE-01 fix: Uses service layer instead of directly accessing repository.
     */
    @GetMapping
    public ResponseEntity<?> getAllAppointments() {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Admin role required."));
        }
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /**
     * Single appointment by ID.
     * SEC-05 fix: only the patient/doctor of this appointment or admin can read it.
     * CODE-01 fix: Uses service layer instead of directly accessing repository.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Appointment> aptOpt = appointmentService.getAppointmentById(id);
        return aptOpt
                .map(apt -> {
                    if (!isAdmin(currentUser) && !isOwner(currentUser, apt)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .<Object>body(Map.of("error", "Access denied."));
                    }
                    return ResponseEntity.<Object>ok(apt);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Patient: their own appointments only.
     * SEC-05 fix: a patient may only fetch their own patientId.
     * CODE-01 fix: Uses service layer instead of directly accessing repository.
     */
    @GetMapping("/patient")
    public ResponseEntity<?> getByPatient(@RequestParam Long patientId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!isAdmin(currentUser)) {
            Patient p = currentUser.getPatient();
            if (p == null || !p.getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. You can only view your own appointments."));
            }
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    /**
     * Doctor: their own appointments only.
     * SEC-05 fix: a doctor may only fetch their own doctorId.
     * CODE-01 fix: Uses service layer instead of directly accessing repository.
     */
    @GetMapping("/doctor")
    public ResponseEntity<?> getByDoctor(@RequestParam Long doctorId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!isAdmin(currentUser)) {
            Doctor d = currentUser.getDoctor();
            if (d == null || !d.getId().equals(doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. You can only view your own appointments."));
            }
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
    }

    /**
     * Doctor marks appointment as COMPLETED.
     * SEC-05 fix: only the assigned doctor (or admin) can mark complete.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> markComplete(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) return ResponseEntity.notFound().build();

        if (!isAdmin(currentUser)) {
            Doctor d = currentUser.getDoctor();
            if (d == null || !d.getId().equals(apt.getDoctorId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error",
                                "Access denied. Only the assigned doctor can mark this appointment complete."));
            }
        }

        if (apt.getStatus() != AppointmentStatus.BOOKED) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only BOOKED appointments can be marked complete."));
        }
        apt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(apt);
        return ResponseEntity.ok(Map.of("message", "Appointment marked as completed."));
    }

    /**
     * Doctor uploads prescription (PDF or image).
     * SEC-05 fix: only the assigned doctor (or admin) can upload.
     * MISSING-03 fix: validate file type (only PDF, JPG, JPEG, PNG allowed)
     */
    @PutMapping("/{id}/prescription")
    public ResponseEntity<?> uploadPrescription(@PathVariable Long id,
                                                @RequestParam("file") MultipartFile file) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) return ResponseEntity.notFound().build();

        if (!isAdmin(currentUser)) {
            Doctor d = currentUser.getDoctor();
            if (d == null || !d.getId().equals(apt.getDoctorId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error",
                                "Access denied. Only the assigned doctor can upload a prescription."));
            }
        }

        // MISSING-03: Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedPrescriptionType(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid file type. Only PDF, JPG, JPEG, and PNG files are allowed."));
        }

        try {
            apt.setPrescriptionFile(file.getBytes());
            appointmentRepository.save(apt);
            return ResponseEntity.ok(Map.of("message", "Prescription uploaded successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * MISSING-03 helper: Check if the uploaded file is a valid prescription format.
     * Allowed: application/pdf, image/jpeg, image/jpg, image/png
     */
    private boolean isAllowedPrescriptionType(String contentType) {
        return contentType.equalsIgnoreCase("application/pdf") ||
               contentType.equalsIgnoreCase("image/jpeg") ||
               contentType.equalsIgnoreCase("image/jpg") ||
               contentType.equalsIgnoreCase("image/png");
    }

    /**
     * Download prescription as base64.
     * SEC-05 fix: only the patient/doctor of this appointment (or admin) can download.
     */
    @GetMapping("/{id}/prescription")
    public ResponseEntity<?> getPrescription(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) return ResponseEntity.notFound().build();

        if (!isAdmin(currentUser) && !isOwner(currentUser, apt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied."));
        }

        if (apt.getPrescriptionFile() == null) {
            return ResponseEntity.status(404).body(Map.of("error", "No prescription uploaded yet."));
        }
        String b64 = java.util.Base64.getEncoder().encodeToString(apt.getPrescriptionFile());
        return ResponseEntity.ok(Map.of("prescription", b64));
    }

    /**
     * FEAT-01 fix: Patient/Doctor cancels an appointment with optional refund.
     * Only the patient/doctor of this appointment (or admin) can cancel.
     * Processes refund if appointment was BOOKED (payment confirmed).
     *
     * Refund logic:
     * - Doctor cancels: Full refund (100% of consultation fee)
     * - Patient cancels: Partial refund (50% of consultation fee)
     * - Appointment was PENDING_PAYMENT: No refund
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "User initiated cancellation") String reason) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Fetch appointment to verify ownership
        Optional<Appointment> aptOpt = appointmentService.getAppointmentById(id);
        if (aptOpt.isEmpty()) return ResponseEntity.notFound().build();

        Appointment apt = aptOpt.get();

        // SEC-05: Verify ownership — only the patient/doctor or admin can cancel
        if (!isAdmin(currentUser) && !isOwner(currentUser, apt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. You can only cancel your own appointments."));
        }

        // Determine who is cancelling: doctor (full refund) or patient (50% refund)
        Doctor doctorUser = currentUser.getDoctor();
        boolean isDoctorCancelling = doctorUser != null && doctorUser.getId().equals(apt.getDoctorId());

        // Delegate to service for cancellation & refund processing
        Map<String, Object> result = appointmentService.cancelAppointment(id, reason, isDoctorCancelling);

        // Check for errors
        if (result.containsKey("status") && result.get("status") instanceof Integer) {
            int statusCode = (Integer) result.remove("status");
            return ResponseEntity.status(statusCode).body(result);
        }

        return ResponseEntity.ok(result);
    }
}
