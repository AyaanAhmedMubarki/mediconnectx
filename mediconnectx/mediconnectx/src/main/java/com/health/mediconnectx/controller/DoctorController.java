package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.DoctorService;
import com.health.mediconnectx.dto.DoctorDTO;
import com.health.mediconnectx.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // ── Helper ──────────────────────────────────────────────────────────
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }

    /**
     * SEC-09 fix: Get own doctor profile.
     * Email is extracted from JWT, not accepted as request parameter.
     * Prevents any user from reading arbitrary doctor profiles.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getDoctorProfile() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication required.");
            }
            DoctorDTO doctorProfile = doctorService.getDoctorByEmail(currentUser.getEmail());
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor profile not found.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> getDoctorById(@RequestParam Long id) {
        try {
            DoctorDTO doctorProfile = doctorService.getDoctorById(id);
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDoctor() {
        try {
            List<DoctorDTO> doctorProfile = doctorService.getAllDoctor();
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * GET /api/doctor/specializations
     * Returns a sorted list of distinct specialisations across all doctor profiles.
     * Null/blank category is normalised to "General Practitioner".
     */
    @GetMapping("/specializations")
    public ResponseEntity<List<String>> getSpecializations() {
        return ResponseEntity.ok(doctorService.getSpecializations());
    }

    /**
     * GET /api/doctor/available?date=yyyy-MM-dd&specialization=Cardiologist
     * Returns doctors filtered by specialisation and/or date availability.
     * - specialization omitted → no spec filter
     * - date omitted           → no availability filter (all matching spec)
     * - date present           → only doctors with ≥1 OPEN slot on that date
     */
    @GetMapping("/available")
    public ResponseEntity<List<DoctorDTO>> getAvailableDoctors(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String specialization) {
        return ResponseEntity.ok(doctorService.getAvailableDoctors(specialization, date));
    }

    /**
     * SEC-09 fix: Update own doctor profile.
     * Email is extracted from JWT, not accepted as request parameter.
     * Prevents any user from updating arbitrary doctor profiles.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateDoctorProfile(
            @RequestParam("DoctorDTO") String doctorDTO,
            @RequestParam(value = "imageLink", required = false) MultipartFile imageLink) {

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication required.");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            DoctorDTO doctorDTOObject;
            try {
                doctorDTOObject = objectMapper.readValue(doctorDTO, DoctorDTO.class);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error parsing doctorDTO: " + e.getMessage());
            }

            byte[] imageBytes = null;
            if (imageLink != null && !imageLink.isEmpty()) {
                try {
                    imageBytes = imageLink.getBytes();
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
                }
            }

            doctorService.updateDoctorByEmail(currentUser.getEmail(), doctorDTOObject, imageBytes);
            return ResponseEntity.ok("Doctor profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile: " + e.getMessage());
        }
    }
}
