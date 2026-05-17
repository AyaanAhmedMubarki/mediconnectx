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

        // Resolve role and roleId
        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("UNKNOWN");
        Long roleId = 0L;
        if ("ADMIN".equalsIgnoreCase(role)) {
            roleId = user.getAdmin().getId();
        } else if ("PATIENT".equalsIgnoreCase(role)) {
            roleId = user.getPatient().getId();
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            roleId = user.getDoctor().getId();
        }

        // Resolve display name — fall back to email prefix if name was never saved
        String name = (user.getName() != null && !user.getName().isBlank())
                ? user.getName()
                : user.getEmail().split("@")[0];

        String message = "Login successful for user: " + name;

        return ResponseEntity.ok(new AuthResponse(token, message, name, role, roleId));
    }
}
