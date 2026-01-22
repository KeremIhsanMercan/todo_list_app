package com.example.todo_app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "todo_items")
public class TodoItem {
    
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
    
    @Column(nullable = false)
    private String status = "NOT_STARTED"; // NOT_STARTED, IN_PROGRESS, COMPLETED
    
    @Column(name = "deadline")
    private LocalDate deadline;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    @JsonIgnore
    private TodoList todoList;
    
    // Dependency: items that this item depends on
    @ManyToMany
    @JoinTable(
        name = "todo_item_dependencies",
        joinColumns = @JoinColumn(name = "dependent_item_id"),
        inverseJoinColumns = @JoinColumn(name = "dependency_item_id")
    )
    private Set<TodoItem> dependencies = new HashSet<>();
    
    // Items that depend on this item
    @ManyToMany(mappedBy = "dependencies")
    @JsonIgnore
    private Set<TodoItem> dependents = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public TodoItem() {}
    
    public TodoItem(String name, String description, LocalDate deadline, TodoList todoList) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.todoList = todoList;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getDeadline() {
        return deadline;
    }
    
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public TodoList getTodoList() {
        return todoList;
    }
    
    public void setTodoList(TodoList todoList) {
        this.todoList = todoList;
    }
    
    public Set<TodoItem> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(Set<TodoItem> dependencies) {
        this.dependencies = dependencies;
    }
    
    public Set<TodoItem> getDependents() {
        return dependents;
    }
    
    public void setDependents(Set<TodoItem> dependents) {
        this.dependents = dependents;
    }
    
    public boolean isExpired() {
        if (deadline == null || "COMPLETED".equals(status)) {
            return false;
        }
        return LocalDate.now().isAfter(deadline);
    }
    
    public boolean canBeCompleted() {
        if (dependencies.isEmpty()) {
            return true;
        }
        return dependencies.stream()
            .allMatch(dep -> "COMPLETED".equals(dep.getStatus()));
    }
}
