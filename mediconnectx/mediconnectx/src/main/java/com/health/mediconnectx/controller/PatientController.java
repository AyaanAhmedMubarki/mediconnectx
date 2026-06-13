package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.PatientService;
import com.health.mediconnectx.dto.PatientDTO;
import com.health.mediconnectx.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // ── Helper ──────────────────────────────────────────────────────────
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }

    /**
     * SEC-09 fix: Get own patient profile.
     * Email is extracted from JWT, not accepted as request parameter.
     * Prevents any user from reading arbitrary patient profiles.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getPatientProfile() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication required.");
            }
            PatientDTO patientProfile = patientService.getPatientByEmail(currentUser.getEmail());
            return ResponseEntity.ok(patientProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient profile not found.");
        }
    }

    /**
     * SEC-09 fix: Update own patient profile.
     * Email is extracted from JWT, not accepted as request parameter.
     * Prevents any user from updating arbitrary patient profiles.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updatePatientProfile(@RequestParam("PatientDTO") String patientDTO,
                                                  @RequestParam(value = "imageLink", required = false) MultipartFile imageLink){
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication required.");
            }

            // Convert JSON string to PatientDTO
            ObjectMapper objectMapper = new ObjectMapper();
            PatientDTO patientDTOObject;
            try {
                patientDTOObject = objectMapper.readValue(patientDTO, PatientDTO.class);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error parsing patientDTO: " + e.getMessage());
            }

            byte[] imageBytes = null;

            // If a profile image is uploaded, convert it to byte[] for processing
            if (imageLink != null && !imageLink.isEmpty()) {
                try {
                    imageBytes = imageLink.getBytes();
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
                }
            }

            // Pass the DTO and image bytes to the service for updating the profile
            patientService.updatePatientByEmail(currentUser.getEmail(), patientDTOObject, imageBytes);

            return ResponseEntity.ok("Patient profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile: " + e.getMessage());
        }
    }

    @GetMapping("/profile/search")
    public ResponseEntity<?> getPatientByPatientId(@RequestParam Long id) {
        try {
            PatientDTO patientProfile = patientService.getPatientById(id);
            return ResponseEntity.ok(patientProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NO Patient found");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPatients() {
        try {
            List<PatientDTO> patientProfile = patientService.getAllPatient();
            return ResponseEntity.ok(patientProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No patients found");
        }
    }

}


