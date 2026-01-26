package com.kerem.todoApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 20, message = "Error: Username must be between 3 and 20 characters!")
    private String username;
    
    @NotBlank(message = "Error: New email can not be empty!")
    @Size(max = 50)
    @Email(message = "Error: Invalid email format!")
    private String email;
    
    @NotBlank(message = "Error: Password is required for verification!")
    private String password; // Current password to verify identity
}

