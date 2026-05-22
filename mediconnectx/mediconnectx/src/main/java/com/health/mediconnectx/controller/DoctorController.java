package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.DoctorService;
import com.health.mediconnectx.dto.DoctorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/profile")
    public ResponseEntity<?> getDoctorByEmail(@RequestParam String email) {
        try {
            DoctorDTO doctorProfile = doctorService.getDoctorByEmail(email);
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " not found.");
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

    @PutMapping("/profile")
    public ResponseEntity<?> updateDoctorByEmail(
            @RequestParam String email,
            @RequestParam("DoctorDTO") String doctorDTO,
            @RequestParam(value = "imageLink", required = false) MultipartFile imageLink) {

        ObjectMapper objectMapper = new ObjectMapper();
        DoctorDTO doctorDTOObject;
        try {
            doctorDTOObject = objectMapper.readValue(doctorDTO, DoctorDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing patientDTO: " + e.getMessage());
        }

        byte[] imageBytes = null;
        if (imageLink != null && !imageLink.isEmpty()) {
            try {
                imageBytes = imageLink.getBytes();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
            }
        }

        doctorService.updateDoctorByEmail(email, doctorDTOObject, imageBytes);
        return ResponseEntity.ok("Doctor profile updated successfully!");
    }
}
