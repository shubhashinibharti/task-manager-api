package com.taskmanager.task_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

// Intercepts every HTTP request and checks for a valid JWT token
// Runs once per request before reaching the controller
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // JwtUtil injected by Spring via constructor (Dependency Injection)
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Read the Authorization header from the request
        // Expected format: "Bearer eyJhbGci..."
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Remove "Bearer " prefix (7 chars) to get just the token
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                // Extract user info from the token
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // Tell Spring Security "this user is authenticated"
                // null = no credentials needed (we already verified via token)
                // ROLE_ prefix required by Spring Security convention
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // Store authentication in context for this request
                // Spring Security reads this to know who the current user is
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Always pass request to next filter/controller
        // If no valid token was found, Spring Security will block with 401
        filterChain.doFilter(request, response);
    }
}