package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.RegistrationService;
import com.health.mediconnectx.dto.RegistrationDTO;
import com.health.mediconnectx.entity.Registration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // GET: Fetch all registrations
    @GetMapping("/all")
    public ResponseEntity<?> getAllRegistrations() {
        try {
            List<Registration> registrations = registrationService.getAllRegistrations();
            return ResponseEntity.ok(registrations);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events available currently");
        }
    }

    // POST: Request a new registration — returns the saved Registration so the frontend has the id
    @PostMapping("/create")
    public ResponseEntity<?> createRegistration(@RequestParam("registrationDTO") String registrationDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationDTO registrationDTOObject;
        try {
            registrationDTOObject = objectMapper.readValue(registrationDTO, RegistrationDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing registrationDTO: " + e.getMessage());
        }
        Registration saved = registrationService.createRegistration(registrationDTOObject);
        return ResponseEntity.ok(saved);
    }

    // PUT: Update registration status (PENDING → APPROVED or REJECTED only; final states are locked)
    @PutMapping("/update")
    public ResponseEntity<?> updateRegistration(@RequestParam Long id, @RequestParam String status) {
        registrationService.updateRegistration(id, status);
        return ResponseEntity.ok("Registration status updated to " + status);
    }

    // DELETE: Cancel registration (only allowed when status is PENDING)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);
        return ResponseEntity.ok("Registration cancelled successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<?> getRegistration(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long patientId) {

        if (eventId != null && patientId != null) {
            // Search by both eventId and patientId
            Optional<Registration> registration = registrationService.findByEventIdAndPatientId(eventId, patientId);
            return registration.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } else if (eventId != null) {
            // Search by eventId only
            List<Registration> registrations = registrationService.findByEventId(eventId);
            return ResponseEntity.ok(registrations);
        } else if (patientId != null) {
            // Search by patientId only
            List<Registration> registrations = registrationService.findByPatientId(patientId);
            return ResponseEntity.ok(registrations);
        } else {
            return ResponseEntity.badRequest().body("Either eventId or patientId must be provided.");
        }
    }
}


