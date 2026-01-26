package com.kerem.todoApp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todo_items")
@Getter
@Setter
@NoArgsConstructor
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 1, max = 200)
    @Column(nullable = false)
    private String name;
    
    @Size(max = 1000)
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.NOT_STARTED;
    
    @Column(name = "deadline")
    private LocalDate deadline;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    @JsonIgnore
    private ItemList list;
    
    // Dependency: items that this item depends on
    @ManyToMany
    @JoinTable(
        name = "todo_item_dependencies",
        joinColumns = @JoinColumn(name = "dependent_item_id"),
        inverseJoinColumns = @JoinColumn(name = "dependency_item_id")
    )
    private Set<Item> dependencies = new HashSet<>();
    
    // Items that depend on this item
    @ManyToMany(mappedBy = "dependencies")
    @JsonIgnore
    private Set<Item> dependents = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        // Auto-set completedAt when status becomes COMPLETED
        if (ItemStatus.COMPLETED.equals(this.status) && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        // Auto-clear completedAt when status changes from COMPLETED to something else
        else if (!ItemStatus.COMPLETED.equals(this.status) && this.completedAt != null) {
            this.completedAt = null;
        }
    }
    
    public Item(String name, String description, LocalDate deadline, 
                com.kerem.todoApp.model.ItemList list) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.list = list;
    }
    
    public boolean isExpired() {
        if (deadline == null || ItemStatus.COMPLETED.equals(status)) {
            return false;
        }
        return LocalDate.now().isAfter(deadline);
    }
    
    public boolean canBeCompleted() {
        if (dependencies.isEmpty()) {
            return true;
        }
        return dependencies.stream()
            .allMatch(dep -> ItemStatus.COMPLETED.equals(dep.getStatus()));
    }
}

