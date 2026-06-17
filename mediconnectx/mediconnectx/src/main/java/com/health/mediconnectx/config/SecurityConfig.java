package com.health.mediconnectx.config;

import com.health.mediconnectx.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (stateless REST API uses JWT, not cookies)
                .csrf(csrf -> csrf.disable())

                // Enable CORS (uses CorsConfig bean)
                .cors(Customizer.withDefaults())

                // Stateless sessions — JWT handles authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public: registration and login only
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public: contact form (unauthenticated visitors can submit)
                        .requestMatchers("/api/contact/**").permitAll()

                        // SEC-03 fix: Events — GET requires login; write operations require ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/events/**").authenticated()
                        .requestMatchers("/api/events/**").hasRole("ADMIN")

                        // SEC-03 fix: Payments — must be authenticated (ownership checked in controller)
                        .requestMatchers("/api/payments/**").authenticated()

                        // Medicines — admin only (for management), doctors and patients can read
                        .requestMatchers(HttpMethod.POST, "/api/medicines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/medicines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/medicines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/medicines/**").authenticated()

                        // Doctor Locations — doctors can write, everyone can read
                        .requestMatchers(HttpMethod.POST, "/api/doctor-locations/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/doctor-locations/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/doctor-locations/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/doctor-locations/**").authenticated()

                        // Offline Slots — doctors can create/delete, patients can book, everyone can read
                        .requestMatchers(HttpMethod.POST, "/api/offline-slots/*/book").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/offline-slots/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/offline-slots/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/offline-slots/**").authenticated()

                        // Medical Records — authenticated users only
                        .requestMatchers("/api/medical-records/**").authenticated()

                        // Prescriptions — authenticated users only
                        .requestMatchers("/api/prescriptions/**").authenticated()

                        // Tests — authenticated users only
                        .requestMatchers("/api/tests/**").authenticated()

                        // Registrations — must be authenticated (ownership checked in controller)
                        .requestMatchers("/api/registration/**").authenticated()

                        // Appointments — must be authenticated (ownership checked in controller)
                        .requestMatchers("/api/appointments/**").authenticated()

                        // Video calls — must be authenticated
                        .requestMatchers("/api/video/**").authenticated()

                        // Everything else — must be authenticated
                        .anyRequest().authenticated()
                )

                // Add JWT filter before Spring's username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
