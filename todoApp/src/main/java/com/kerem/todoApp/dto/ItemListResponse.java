package com.kerem.todoApp.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ItemListResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private int itemCount;
}
