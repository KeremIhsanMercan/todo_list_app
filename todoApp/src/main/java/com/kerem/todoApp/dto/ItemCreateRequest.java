package com.kerem.todoApp.dto;

import java.time.LocalDate;
import com.kerem.todoApp.model.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemCreateRequest {
    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 200, message = "Item name must be between 1 and 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private LocalDate deadline;
    private ItemStatus status;
}
