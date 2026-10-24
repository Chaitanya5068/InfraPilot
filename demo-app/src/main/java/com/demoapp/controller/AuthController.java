package com.demoapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Simple demo token generation for Task Tracker
        if ("developer".equalsIgnoreCase(request.username()) || "admin".equalsIgnoreCase(request.username())) {
            return ResponseEntity.ok(Map.of(
                    "token", "demo-jwt-token-task-tracker-" + System.currentTimeMillis(),
                    "username", request.username(),
                    "status", "AUTHENTICATED"
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid demo username or password"));
    }
}
