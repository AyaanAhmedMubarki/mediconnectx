package com.health.mediconnectx.services;

import com.health.mediconnectx.dto.LoginRequest;
import com.health.mediconnectx.entity.*;
import com.health.mediconnectx.dto.RegisterRequest;
import com.health.mediconnectx.exception.ApiException;
import com.health.mediconnectx.repository.RoleRepository;
import com.health.mediconnectx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()) != null) { // Check for duplicate email
            throw new ApiException("This email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail()); // Set email
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        System.out.println(request.getRole());


        // Determine role based on input
        Role userRole = roleRepository.findByName(request.getRole().toUpperCase());
        if (userRole == null) {
            throw new ApiException("Role not found");
        }

        // Special handling for ADMIN role
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            // Create and populate the Admin entity
            Admin admin = new Admin();
            admin.setUser(user); // Link admin to the user
            // Link admin to user
            user.setAdmin(admin);
        }

        //Special handling for PATIENT role
        if ("PATIENT".equalsIgnoreCase(request.getRole())) {
            // Create and populate the PATIENT entity
            Patient patient = new Patient();
            patient.setUser(user); // Link admin to the user
            // Link admin to user
            user.setPatient(patient);
        }

        //Special handling for DOCTOR role
        if ("DOCTOR".equalsIgnoreCase(request.getRole())) {
            // Create and populate the DOCTOR entity
            Doctor doctor = new Doctor();
            doctor.setUser(user); // Link admin to the user
            // Link admin to user
            user.setDoctor(doctor);
        }

        user.getRoles().add(userRole);


        return userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User authenticateUser(LoginRequest request) {
        // Find the user by username
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            // UNDO CODE-05: Show specific error message (user doesn't exist)
            throw new ApiException("No account exists with this email. Please register.");
        }

        // Compare the plaintext password from the request with the hashed password stored in the database
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());


        if (!passwordMatches) {
            // UNDO CODE-05: Show specific error message (password is wrong)
            throw new ApiException("Invalid password. Please try again.");
        }

        // Return the user if authentication is successful
        return user;
    }

}
