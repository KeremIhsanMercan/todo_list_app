package com.kerem.todoApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerem.todoApp.dto.DeleteAccountRequest;
import com.kerem.todoApp.dto.JwtResponse;
import com.kerem.todoApp.dto.LoginRequest;
import com.kerem.todoApp.dto.MessageResponse;
import com.kerem.todoApp.dto.SignupRequest;
import com.kerem.todoApp.dto.UpdateUserRequest;
import com.kerem.todoApp.service.AuthService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.authenticateUser(
                loginRequest.getUsername(), 
                loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        authService.registerUser(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword());
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PutMapping("/update")
    public ResponseEntity<JwtResponse> updateUser(@Valid @RequestBody UpdateUserRequest updateRequest) {
        JwtResponse response = authService.updateUser(
                updateRequest.getUsername(),
                updateRequest.getEmail(),
                updateRequest.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<MessageResponse> deleteAccount(@Valid @RequestBody DeleteAccountRequest deleteRequest) {
        authService.deleteAccount(deleteRequest.getPassword());
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully!"));
    }
}

