package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.UserRepository;
import com.health.mediconnectx.entity.User;
import com.health.mediconnectx.dto.RegisterRequest;
import com.health.mediconnectx.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;

public interface UserService {
    User registerUser(RegisterRequest request);

    User authenticateUser(LoginRequest request); // Add this method
    @Autowired
    UserRepository userRepository = null;

    public default User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
