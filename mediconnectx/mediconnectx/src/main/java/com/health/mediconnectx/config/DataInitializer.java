package com.health.mediconnectx.config;

import com.health.mediconnectx.entity.Role;
import com.health.mediconnectx.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            for (String roleName : List.of("ADMIN", "DOCTOR", "PATIENT")) {
                if (roleRepository.findByName(roleName) == null) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                }
            }
        };
    }
}
