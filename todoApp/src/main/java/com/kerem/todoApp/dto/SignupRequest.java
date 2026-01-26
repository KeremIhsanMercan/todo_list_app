package com.kerem.todoApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank(message = "Error: Username, email, and password can not be empty!")
    @Size(min = 3, max = 20, message = "Error: Username must be between 3 and 20 characters!")
    private String username;
    
    @NotBlank(message = "Error: Username, email, and password can not be empty!")
    @Size(max = 50)
    @Email(message = "Error: Invalid email format!")
    private String email;
    
    @NotBlank(message = "Error: Username, email, and password can not be empty!")
    @Size(min = 6, max = 100, message = "Error: Password must be at least 6 characters!")
    private String password;
}

