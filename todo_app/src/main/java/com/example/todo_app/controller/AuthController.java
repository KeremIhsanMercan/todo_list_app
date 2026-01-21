package com.example.todo_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo_app.dto.DeleteAccountRequest;
import com.example.todo_app.dto.JwtResponse;
import com.example.todo_app.dto.LoginRequest;
import com.example.todo_app.dto.MessageResponse;
import com.example.todo_app.dto.SignupRequest;
import com.example.todo_app.dto.UpdateUserRequest;
import com.example.todo_app.model.User;
import com.example.todo_app.repository.UserRepository;
import com.example.todo_app.security.JwtUtils;
import com.example.todo_app.security.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail()));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // Validate input

        if (signUpRequest.getUsername() == null || signUpRequest.getEmail() == null || signUpRequest.getPassword() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username, email, and password can not be empty!"));
        }

        if (signUpRequest.getUsername().length() < 3 || signUpRequest.getUsername().length() > 20) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username must be between 3 and 20 characters!"));
        }

        if (!signUpRequest.getEmail().matches("\\S+@\\S+\\.\\S+")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid email format!"));
        }

        if (signUpRequest.getPassword().length() < 6 || signUpRequest.getPassword().length() > 100) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Password must be at least 6 characters!"));
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
        
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest updateRequest, 
                                        Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getUsername() == null || updateRequest.getEmail() == null || updateRequest.getPassword() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username, email, and password can not be empty!"));
        }
        
        if (updateRequest.getUsername().length() < 3 || updateRequest.getUsername().length() > 20) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username must be between 3 and 20 characters!"));
        }

        if (!updateRequest.getEmail().matches("\\S+@\\S+\\.\\S+")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid email format!"));
        }

        // Verify password
        if (!encoder.matches(updateRequest.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Incorrect password!"));
        }
        
        // Check if new username is already taken by another user
        if (!user.getUsername().equals(updateRequest.getUsername()) && 
            userRepository.existsByUsername(updateRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        // Check if new email is already used by another user
        if (!user.getEmail().equals(updateRequest.getEmail()) && 
            userRepository.existsByEmail(updateRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Update user information
        user.setUsername(updateRequest.getUsername());
        user.setEmail(updateRequest.getEmail());
        userRepository.save(user);
        
        // Generate new JWT with updated information
        Authentication newAuth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(updateRequest.getUsername(), updateRequest.getPassword()));
        String jwt = jwtUtils.generateJwtToken(newAuth);
        
        UserDetailsImpl updatedUserDetails = (UserDetailsImpl) newAuth.getPrincipal();
        
        return ResponseEntity.ok(new JwtResponse(jwt,
                updatedUserDetails.getId(),
                updatedUserDetails.getUsername(),
                updatedUserDetails.getEmail()));
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@Valid @RequestBody DeleteAccountRequest deleteRequest,
                                          Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify password
        if (!encoder.matches(deleteRequest.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Incorrect password!"));
        }
        
        // Delete the user (cascade will delete all related data)
        userRepository.delete(user);
        
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully!"));
    }
}
