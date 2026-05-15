package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.PatientService;
import com.health.mediconnectx.dto.PatientDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/profile")
    public ResponseEntity<?> getPatientByEmail(@RequestParam String email) {
        try {
            PatientDTO patientProfile = patientService.getPatientByEmail(email);
            return ResponseEntity.ok(patientProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " not found.");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updatePatientByEmail(@RequestParam String email, @RequestParam("PatientDTO") String patientDTO, // Receive as String and convert to PatientDTO
                                                  @RequestParam(value = "imageLink", required = false) MultipartFile imageLink){
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
                imageBytes = imageLink.getBytes(); // Convert the image to byte[] format
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
            }
        }

        // Pass the DTO and image bytes to the service for updating the profile
        patientService.updatePatientByEmail(email, patientDTOObject, imageBytes);

        return ResponseEntity.ok("Patient profile updated successfully!");
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
    public ResponseEntity<?> getAllAthlete() {
        try {
            List<PatientDTO> patientProfile = patientService.getAllPatient();
            return ResponseEntity.ok(patientProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

}


