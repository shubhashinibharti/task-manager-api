package com.taskmanager.task_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Replaces Spring Security's default configuration with our custom rules
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // JwtAuthFilter injected by Spring — Dependency Injection
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // Defines all security rules for the application
    // @Bean = Spring manages this object (can't use @Component on library classes)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for JWT (CSRF only affects cookie auth)
                .csrf(csrf -> csrf.disable())

                // Don't store sessions — JWT carries identity, server stays stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules:
                // /auth/** = public (register, login — no token needed)
                // Everything else = must have valid JWT token
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Register our JWT filter to run before Spring's default auth filter
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Password encoder bean — BCrypt hashes passwords securely
    // @Bean used because BCryptPasswordEncoder is a library class
    // Used when registering users to hash their password before saving
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}