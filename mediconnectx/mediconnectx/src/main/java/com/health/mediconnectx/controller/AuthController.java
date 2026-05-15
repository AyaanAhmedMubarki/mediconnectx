package com.health.mediconnectx.controller;

import com.health.mediconnectx.dto.AuthResponse;
import com.health.mediconnectx.dto.LoginRequest;
import com.health.mediconnectx.dto.RegisterRequest;
import com.health.mediconnectx.entity.*;
import com.health.mediconnectx.services.UserService;
import com.health.mediconnectx.security.JwtTokenProvider; // Import JWT utility class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // Inject JWT utility

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Authenticate the user
        User user = userService.authenticateUser(request);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // Create a response message
        String message = "Login successful for user: " + user.getName();

        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("UNKNOWN");
        Long roleId = 0L;
        if ("ADMIN".equalsIgnoreCase(role)){
            Admin admin = user.getAdmin();
            roleId = admin.getId();
        }else if ("PATIENT".equalsIgnoreCase(role)){
            Patient patient = user.getPatient();
            roleId = patient.getId();
        }else  if ("DOCTOR".equalsIgnoreCase(role)){
            Doctor doctor = user.getDoctor();
            roleId = doctor.getId();
        }
        else{
            System.out.println("No role type found");
        }
        // Return token and message in response
        return ResponseEntity.ok(new AuthResponse(token, message, role, roleId)); // Create an AuthResponse class to hold the token and message
    }
}
