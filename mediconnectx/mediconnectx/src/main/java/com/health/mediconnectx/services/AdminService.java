package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.AdminRepository;
import com.health.mediconnectx.repository.UserRepository;
import com.health.mediconnectx.dto.AdminDTO;
import com.health.mediconnectx.entity.Admin;
import com.health.mediconnectx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class AdminService {



    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    // Fetch Admin Profile by email
    public AdminDTO getAdminProfileByEmail(String email) {

        // Find the user by email
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }


        // Find the associated Admin profile by userId
        Admin admin = adminRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Admin profile not found"));

        // Return AdminDTO with user and admin data
        AdminDTO adminDTO = new AdminDTO(
                admin.getId(),
                user.getName(),
                user.getEmail(),
                admin.getExperience(),
                // Assign the profile image directly, since it's already Base64-encoded
                admin.getProfileImage() // No need for Base64 encoding
        );

        return adminDTO;
    }

    // Update Admin Profile by email
    public void updateAdminProfileByEmail(String email, AdminDTO adminDTO, byte[] imageData) {
        // Find the user by email
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Find the associated Admin profile by userId
        Admin admin = adminRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Admin profile not found"));

        // Update the username in the User table (from the AdminDTO)
        user.setName(adminDTO.getName());
        admin.setExperience(adminDTO.getExperience());

        // Convert byte[] to Base64 string and set it in the Admin entity
        if (imageData != null && imageData.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            admin.setProfileImage(base64Image);
        }

        // Save the updated entities
        userRepository.save(user);  // Save user with the updated name
        adminRepository.save(admin);  // Save admin with the updated data
    }
}

