package com.taskmanager.task_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

// Utility class for creating and validating JWT tokens
// Spring manages this as a singleton via @Component
@Component
public class JwtUtil {

    // Secret key used to sign and verify tokens
    // Generated once at startup — same key for sign and verify
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token validity period — 24 hours in milliseconds
    private final long EXPIRATION = 1000 * 60 * 60 * 24;

    // Creates a JWT token containing username and role
    // Called after successful login — token returned to client
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)           // who this token belongs to
                .claim("role", role)            // user's role stored inside token
                .setIssuedAt(new Date())         // when token was created
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // expiry
                .signWith(key)                  // sign with secret key
                .compact();                     // build final token string
    }

    // Reads username from inside the token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Reads role from inside the token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Returns true if token signature is valid and not expired
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false; // expired, tampered, or malformed
        }
    }

    // Decodes and verifies the token using secret key
    // Throws JwtException if token is invalid or expired
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)             // use same key that signed the token
                .build()
                .parseClaimsJws(token)          // decode and verify signature
                .getBody();                     // return the payload data
    }
}