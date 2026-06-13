package com.health.mediconnectx.controller;

import com.health.mediconnectx.dto.RegistrationDTO;
import com.health.mediconnectx.entity.Registration;
import com.health.mediconnectx.entity.User;
import com.health.mediconnectx.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // ── Helpers ──────────────────────────────────────────────────────────

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getName()));
    }

    // ── Endpoints ────────────────────────────────────────────────────────

    /** Admin only: fetch all registrations */
    @GetMapping("/all")
    public ResponseEntity<?> getAllRegistrations() {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Admin role required.");
        }
        try {
            List<Registration> registrations = registrationService.getAllRegistrations();
            return ResponseEntity.ok(registrations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No registrations found.");
        }
    }

    /** Create a new registration (any authenticated user — patient or doctor) */
    @PostMapping("/create")
    public ResponseEntity<?> createRegistration(@RequestParam("registrationDTO") String registrationDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationDTO registrationDTOObject;
        try {
            registrationDTOObject = objectMapper.readValue(registrationDTO, RegistrationDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing registrationDTO: " + e.getMessage());
        }
        try {
            Registration saved = registrationService.createRegistration(registrationDTOObject);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Approve or reject a registration.
     * SEC-07 fix: only ADMIN may change registration status.
     * Also validates that only APPROVED or REJECTED are accepted.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateRegistration(@RequestParam Long id, @RequestParam String status) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only admins can approve or reject registrations.");
        }
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.badRequest()
                    .body("Invalid status. Allowed values: APPROVED, REJECTED.");
        }
        registrationService.updateRegistration(id, status);
        return ResponseEntity.ok("Registration status updated to " + status);
    }

    /**
     * Cancel a registration.
     * SEC-07 fix: only the registrant themselves (or an admin) may cancel.
     * The registrant is identified by comparing their patient/doctor profile ID
     * against the registration's patientId field (which stores the registrant's
     * profile ID regardless of whether they are a patient or a doctor).
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Fetch the registration first so we can check ownership
        Optional<Registration> regOpt = registrationService.findById(id);
        if (regOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not found.");
        }
        Registration registration = regOpt.get();

        if (!isAdmin(currentUser)) {
            // Determine the current user's profile ID
            // (patients use patient.id; doctors use doctor.id — both stored in patientId column)
            Long currentProfileId = null;
            if (currentUser.getPatient() != null) {
                currentProfileId = currentUser.getPatient().getId();
            } else if (currentUser.getDoctor() != null) {
                currentProfileId = currentUser.getDoctor().getId();
            }

            if (currentProfileId == null || !currentProfileId.equals(registration.getPatientId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. You can only cancel your own registrations.");
            }
        }

        try {
            registrationService.deleteRegistration(id);
            return ResponseEntity.ok("Registration cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Search registrations by eventId and/or profileId (UX-02 fix).
     * Also accepts legacy "patientId" parameter for backward compatibility.
     *
     * The profileId actually stores a profile ID that works for both patients and doctors:
     * - Patient registration: profileId = patient.id, userRole = "PATIENT"
     * - Doctor registration:  profileId = doctor.id, userRole = "DOCTOR"
     *
     * Optional userRole param ("PATIENT" or "DOCTOR") narrows results so that
     * a patient and a doctor who share the same numeric ID do not see each
     * other's registrations (both tables start their auto-increment at 1).
     */
    @GetMapping("/search")
    public ResponseEntity<?> getRegistration(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) Long patientId,  // Deprecated: use profileId instead
            @RequestParam(required = false) String userRole) {

        // UX-02 fix: Support both profileId (new) and patientId (legacy) for backward compatibility
        Long id = profileId != null ? profileId : patientId;

        if (eventId != null && id != null) {
            Optional<Registration> registration =
                    registrationService.findByEventIdAndPatientId(eventId, id);
            return registration.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } else if (eventId != null) {
            List<Registration> registrations = registrationService.findByEventId(eventId);
            return ResponseEntity.ok(registrations);
        } else if (id != null && userRole != null) {
            // Role-scoped lookup: prevents cross-role ID collisions
            List<Registration> registrations =
                    registrationService.findByPatientIdAndUserRole(id, userRole.toUpperCase());
            return ResponseEntity.ok(registrations);
        } else if (id != null) {
            List<Registration> registrations = registrationService.findByPatientId(id);
            return ResponseEntity.ok(registrations);
        } else {
            return ResponseEntity.badRequest().body("Either eventId or profileId must be provided.");
        }
    }
}
