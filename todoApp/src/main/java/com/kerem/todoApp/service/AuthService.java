package com.kerem.todoApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kerem.todoApp.dto.JwtResponse;
import com.kerem.todoApp.exception.AuthenticationException;
import com.kerem.todoApp.exception.ResourceAlreadyExistsException;
import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.model.User;
import com.kerem.todoApp.repository.UserRepository;
import com.kerem.todoApp.security.JwtUtils;
import com.kerem.todoApp.security.UserDetailsImpl;

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
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email is already in use!");
        }
        
        // Create new user's account
        User user = new User(username, email, encoder.encode(password));
        userRepository.save(user);
    }
    
    /**
     * Update user information
     */
    public JwtResponse updateUser(Authentication authentication, String newUsername, String newEmail, String password) {
        Long userId = getUserIdFromAuthentication(authentication);
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify password
        if (!encoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Incorrect password!");
        }
        
        // Check if new username is already taken by another user
        if (!user.getUsername().equals(newUsername) && 
            userRepository.existsByUsername(newUsername)) {
            throw new ResourceAlreadyExistsException("Username is already taken!");
        }
        
        // Check if new email is already used by another user
        if (!user.getEmail().equals(newEmail) && 
            userRepository.existsByEmail(newEmail)) {
            throw new ResourceAlreadyExistsException("Email is already in use!");
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
    public void deleteAccount(Authentication authentication, String password) {
        Long userId = getUserIdFromAuthentication(authentication);
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify password
        if (!encoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Incorrect password!");
        }
        
        // Delete the user (cascade will delete all related data)
        userRepository.delete(user);
    }
    
    /**
     * Extract user ID from authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}

