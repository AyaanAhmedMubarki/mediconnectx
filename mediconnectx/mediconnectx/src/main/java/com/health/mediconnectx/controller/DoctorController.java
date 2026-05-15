package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.DoctorService;
import com.health.mediconnectx.dto.DoctorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/profile")
    public ResponseEntity<?> getDoctorByEmail(@RequestParam String email) {
        try {
            DoctorDTO doctorProfile = doctorService.getDoctorByEmail(email);
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " not found.");
        }
    }
    @GetMapping("/search")
    public ResponseEntity<?> getDoctorById(@RequestParam Long id) {
        try {
            DoctorDTO doctorProfile = doctorService.getDoctorById(id);
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDoctor() {
        try {
            List<DoctorDTO> doctorProfile = doctorService.getAllDoctor();
            return ResponseEntity.ok(doctorProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateDoctorByEmail(@RequestParam String email, @RequestParam("DoctorDTO") String doctorDTO, // Receive as String and convert to DoctorDTO
                                                 @RequestParam(value = "imageLink", required = false) MultipartFile imageLink){
        // Convert JSON string to DoctorDTO
        ObjectMapper objectMapper = new ObjectMapper();
        DoctorDTO doctorDTOObject;
        try {
            doctorDTOObject = objectMapper.readValue(doctorDTO, DoctorDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing patientDTO: " + e.getMessage());
        }

        byte[] imageBytes = null;

        // If a profile image is uploaded, convert it to byte[] for processing
        if (imageLink != null && !imageLink.isEmpty()) {
            try {
                imageBytes = imageLink.getBytes(); // Convert the image to byte[] format
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
            }
        }

        // Pass the DTO and image bytes to the service for updating the profile
        doctorService.updateDoctorByEmail(email, doctorDTOObject, imageBytes);

        return ResponseEntity.ok("Doctor profile updated successfully!");
    }
}

