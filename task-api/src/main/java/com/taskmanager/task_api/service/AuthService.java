package com.taskmanager.task_api.service;

import com.taskmanager.task_api.entity.AppUser;
import com.taskmanager.task_api.repository.UserRepository;
import com.taskmanager.task_api.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register new user
    // Checks duplicate username, hashes password, saves to DB
    public String register (String username, String password, String role){
        if(userRepository.findByUsername(username).isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists"
            );
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt hash
        user.setRole(role);
        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(String username, String password){
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return jwtUtil.generateToken(username, user.getRole());
    }
}
