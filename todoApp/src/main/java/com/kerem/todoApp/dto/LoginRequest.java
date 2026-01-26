package com.kerem.todoApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Error: Username can not be empty!")
    private String username;
    
    @NotBlank(message = "Error: Password can not be empty!")
    private String password;
}

