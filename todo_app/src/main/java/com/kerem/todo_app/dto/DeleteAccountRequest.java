package com.kerem.todo_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank
    @Size(min = 6, max = 40)
    private String password; // Current password to verify identity
}
