package com.kerem.todo_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kerem.todo_app.dto.JwtResponse;
import com.kerem.todo_app.model.User;
import com.kerem.todo_app.repository.UserRepository;
import com.kerem.todo_app.security.JwtUtils;
import com.kerem.todo_app.security.UserDetailsImpl;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * Authenticate user and generate JWT token
     */
    public JwtResponse authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail());
    }
    
    /**
     * Register a new user
     */
    public void registerUser(String username, String email, String password) {
        // Validate input
        if (username == null || email == null || password == null) {
            throw new IllegalArgumentException("Error: Username, email, and password can not be empty!");
        }
        
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Error: Username must be between 3 and 20 characters!");
        }
        
        if (!email.matches("\\S+@\\S+\\.\\S+")) {
            throw new IllegalArgumentException("Error: Invalid email format!");
        }
        
        if (password.length() < 6 || password.length() > 100) {
            throw new IllegalArgumentException("Error: Password must be at least 6 characters!");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }
        
        // Create new user's account
        User user = new User(username, email, encoder.encode(password));
        userRepository.save(user);
    }
    
    /**
     * Update user information
     */
    public JwtResponse updateUser(Long userId, String newUsername, String newEmail, String password) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if password is provided for verification
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Error: Password is required for verification!");
        }
        
        // Validate if new username and email are not empty
        if (newUsername == null || newUsername.isEmpty()) {
            throw new IllegalArgumentException("Error: New username can not be empty!");
        }

        if (newEmail == null || newEmail.isEmpty()) {
            throw new IllegalArgumentException("Error: New email can not be empty!");
        }

        // Validate new username length
        if (newUsername.length() < 3 || newUsername.length() > 20) {
            throw new IllegalArgumentException("Error: Username must be between 3 and 20 characters!");
        }

        // Validate new email format
        if (!newEmail.matches("\\S+@\\S+\\.\\S+")) {
            throw new IllegalArgumentException("Error: Invalid email format!");
        }

        // Verify password
        if (!encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Error: Incorrect password!");
        }
        
        // Check if new username is already taken by another user
        if (!user.getUsername().equals(newUsername) && 
            userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }
        
        // Check if new email is already used by another user
        if (!user.getEmail().equals(newEmail) && 
            userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }
        
        // Update user information
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        userRepository.save(user);
        
        // Generate new JWT with updated information
        Authentication newAuth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(newUsername, password));
        String jwt = jwtUtils.generateJwtToken(newAuth);
        
        UserDetailsImpl updatedUserDetails = (UserDetailsImpl) newAuth.getPrincipal();
        
        return new JwtResponse(jwt,
                updatedUserDetails.getId(),
                updatedUserDetails.getUsername(),
                updatedUserDetails.getEmail());
    }
    
    /**
     * Delete user account
     */
    public void deleteAccount(Long userId, String password) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify password
        if (!encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Error: Incorrect password!");
        }
        
        // Delete the user (cascade will delete all related data)
        userRepository.delete(user);
    }
}
