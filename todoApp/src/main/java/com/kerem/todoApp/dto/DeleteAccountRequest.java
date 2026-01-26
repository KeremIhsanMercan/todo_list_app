package com.kerem.todoApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank(message = "Error: Password is required for verification!")
    @Size(min = 6, max = 40)
    private String password; // Current password to verify identity
}

