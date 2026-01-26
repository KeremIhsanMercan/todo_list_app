package com.kerem.todoApp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.kerem.todoApp.model.ItemStatus;
import lombok.Data;

@Data
public class ItemResponse {
    private Long id;
    private String name;
    private String description;
    private ItemStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long listId;
    private List<DependencyInfo> dependencies;  // Full dependency info with name
    private boolean expired;
    private boolean canBeCompleted;
    
    @Data
    public static class DependencyInfo {
        private Long id;
        private String name;
    }
}
