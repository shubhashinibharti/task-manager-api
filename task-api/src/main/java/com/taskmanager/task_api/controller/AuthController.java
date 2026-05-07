package com.taskmanager.task_api.controller;

import com.taskmanager.task_api.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Public endpoint — no token needed
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@RequestBody  Map<String, String> body) {
        return authService.register(
                body.get("username"),
                body.get("password"),
                body.getOrDefault("role", "USER")
        );
    }

    // Public endpoint — returns JWT token on success
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String token = authService.login(
                body.get("username"),
                body.get("password")
        );
        return Map.of("token", token);
    }




















}
