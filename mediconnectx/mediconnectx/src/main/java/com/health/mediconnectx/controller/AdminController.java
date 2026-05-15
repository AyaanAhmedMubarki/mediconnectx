package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.AdminService;
import com.health.mediconnectx.dto.AdminDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Fetch Admin Profile by email
    @GetMapping("/profile")
    public ResponseEntity<?> getAdminProfile(@RequestParam String email) {
        try {
            AdminDTO adminProfile = adminService.getAdminProfileByEmail(email);
            return ResponseEntity.ok(adminProfile);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " not found.");
        }
    }

    // Update Admin Profile by email (with profile image upload)
    @PutMapping("/profile")
    public ResponseEntity<String> updateAdminProfile(
            @RequestParam String email,
            @RequestParam("adminDTO") String adminDTO, // Receive as String and convert to AdminDTO
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        // Convert JSON string to AdminDTO
        ObjectMapper objectMapper = new ObjectMapper();
        AdminDTO adminDTOObject;
        try {
            adminDTOObject = objectMapper.readValue(adminDTO, AdminDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing adminDTO: " + e.getMessage());
        }

        byte[] imageBytes = null;

        // If a profile image is uploaded, convert it to byte[] for processing
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                imageBytes = profileImage.getBytes(); // Convert the image to byte[] format
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
            }
        }

        // Pass the DTO and image bytes to the service for updating the profile
        adminService.updateAdminProfileByEmail(email, adminDTOObject, imageBytes);

        return ResponseEntity.ok("Admin profile updated successfully!");
    }
}
