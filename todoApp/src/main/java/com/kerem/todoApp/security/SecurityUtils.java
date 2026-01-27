package com.kerem.todoApp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kerem.todoApp.exception.AuthenticationException;

@Component
public class SecurityUtils {
    
    /**
     * Get the current authenticated user ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
    
    /**
     * Get the current authentication object (if needed)
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Get the current user details
     */
    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }
        
        return (UserDetailsImpl) authentication.getPrincipal();
    }
}
