package com.example.todo_app.controller;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo_app.dto.MessageResponse;
import com.example.todo_app.model.TodoItem;
import com.example.todo_app.model.TodoList;
import com.example.todo_app.repository.TodoItemRepository;
import com.example.todo_app.repository.TodoListRepository;
import com.example.todo_app.security.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/todolists/{listId}/items")
@CrossOrigin(origins = "*")
public class TodoItemController {
    
    @Autowired
    private TodoItemRepository todoItemRepository;
    
    @Autowired
    private TodoListRepository todoListRepository;
    
    // Get all items in a todo list with filtering and sorting
    @GetMapping
    public ResponseEntity<?> getTodoItems(
            @PathVariable Long listId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean expired,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            Authentication authentication) {
        
        List<TodoItem> items = todoItemRepository.findByTodoListId(listId);
        
        // Filter by status
        if (status != null && !status.isEmpty()) {
            items = items.stream()
                    .filter(item -> status.equals(item.getStatus()))
                    .collect(Collectors.toList());
        }
        
        // Filter by expired
        if (expired != null) {
            items = items.stream()
                    .filter(item -> expired.equals(item.isExpired()))
                    .collect(Collectors.toList());
        }
        
        // Filter by name
        if (name != null && !name.isEmpty()) {
            items = items.stream()
                    .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // Sort
        if (sortBy != null) {
            Comparator<TodoItem> comparator = getComparator(sortBy);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            items = items.stream().sorted(comparator).collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(items);
    }
    
    // Get item by id
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getTodoItem(@PathVariable Long listId, @PathVariable Long itemId, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify user owns this list
        todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        return todoItemRepository.findByIdAndTodoListId(itemId, listId)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Create new todo item
    @PostMapping
    public ResponseEntity<?> createTodoItem(@PathVariable Long listId,
                                           @Valid @RequestBody TodoItem itemRequest,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        TodoList todoList = todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        TodoItem todoItem = new TodoItem(
                itemRequest.getName(),
                itemRequest.getDescription(),
                itemRequest.getDeadline(),
                todoList
        );
        
        if (itemRequest.getStatus() != null) {
            todoItem.setStatus(itemRequest.getStatus());
        }
        
        TodoItem savedItem = todoItemRepository.save(todoItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }
    
    // Update todo item
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateTodoItem(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           @Valid @RequestBody TodoItem itemDetails,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        return todoItemRepository.findByIdAndTodoListId(itemId, listId)
                .map(item -> {
                    // Rule 1: Cannot mark as COMPLETED if this item's dependencies are not complete
                    if ("COMPLETED".equals(itemDetails.getStatus()) && !item.canBeCompleted()) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Cannot mark as COMPLETED! Dependencies are not complete"));
                    }
                    
                    // Rule 2: Cannot change from COMPLETED to something else if items depending on this are COMPLETED
                    if (!"COMPLETED".equals(itemDetails.getStatus()) && "COMPLETED".equals(item.getStatus())) {
                        // Find items that depend on this item (items that have this item in their dependencies)
                        List<TodoItem> itemsDependingOnThis = todoItemRepository.findByTodoListId(listId).stream()
                                .filter(otherItem -> otherItem.getDependencies().stream()
                                        .anyMatch(dep -> dep.getId().equals(itemId)))
                                .collect(Collectors.toList());
                        
                        // Check if any of those items are COMPLETED
                        boolean hasCompletedDependents = itemsDependingOnThis.stream()
                                .anyMatch(dependent -> "COMPLETED".equals(dependent.getStatus()));
                        
                        if (hasCompletedDependents) {
                            return ResponseEntity.badRequest()
                                    .body(new MessageResponse("Cannot change status! Items depending on this are COMPLETED"));
                        }
                    }
                    
                    item.setName(itemDetails.getName());
                    item.setDescription(itemDetails.getDescription());
                    item.setDeadline(itemDetails.getDeadline());
                    item.setStatus(itemDetails.getStatus());
                    
                    if ("COMPLETED".equals(itemDetails.getStatus())) {
                        item.setCompletedAt(LocalDateTime.now());
                    }
                    
                    TodoItem updatedItem = todoItemRepository.save(item);
                    return ResponseEntity.ok(updatedItem);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Mark item as complete
    @PatchMapping("/{itemId}/complete")
    public ResponseEntity<?> markAsComplete(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        return todoItemRepository.findByIdAndTodoListId(itemId, listId)
                .map(item -> {
                    if (!item.canBeCompleted()) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Cannot complete: Dependencies not satisfied"));
                    }
                    
                    item.setStatus("COMPLETED");
                    item.setCompletedAt(LocalDateTime.now());
                    TodoItem updatedItem = todoItemRepository.save(item);
                    return ResponseEntity.ok(updatedItem);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Add dependency
    @PostMapping("/{itemId}/dependencies/{dependencyId}")
    public ResponseEntity<?> addDependency(@PathVariable Long listId,
                                          @PathVariable Long itemId,
                                          @PathVariable Long dependencyId,
                                          Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        TodoItem item = todoItemRepository.findByIdAndTodoListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        TodoItem dependency = todoItemRepository.findByIdAndTodoListId(dependencyId, listId)
                .orElseThrow(() -> new RuntimeException("Dependency item not found"));
        
        if (itemId.equals(dependencyId)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Item cannot depend on itself"));
        }
        
        // Check for circular dependency
        if (wouldCreateCircularDependency(item, dependency)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("This would create a circular dependency"));
        }
        
        item.getDependencies().add(dependency);
        todoItemRepository.save(item);
        
        return ResponseEntity.ok(new MessageResponse("Dependency added successfully"));
    }
    
    // Remove dependency
    @DeleteMapping("/{itemId}/dependencies/{dependencyId}")
    public ResponseEntity<?> removeDependency(@PathVariable Long listId,
                                             @PathVariable Long itemId,
                                             @PathVariable Long dependencyId,
                                             Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        TodoItem item = todoItemRepository.findByIdAndTodoListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        TodoItem dependency = todoItemRepository.findByIdAndTodoListId(dependencyId, listId)
                .orElseThrow(() -> new RuntimeException("Dependency item not found"));
        
        item.getDependencies().remove(dependency);
        todoItemRepository.save(item);
        
        return ResponseEntity.ok(new MessageResponse("Dependency removed successfully"));
    }
    
    // Delete todo item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteTodoItem(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        todoListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        

        List<TodoItem> itemsDependingOnThis = todoItemRepository.findByTodoListId(listId).stream()
                .filter(item -> item.getDependencies().stream()
                        .anyMatch(dependency -> dependency.getId().equals(itemId)))
                .collect(Collectors.toList());
                
        if (!itemsDependingOnThis.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Cannot delete: Other items depend on this item"));
        }

        return todoItemRepository.findByIdAndTodoListId(itemId, listId)
                .map(item -> {
                    todoItemRepository.delete(item);
                    return ResponseEntity.ok(new MessageResponse("Todo item deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
    
    private Comparator<TodoItem> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(TodoItem::getName);
            case "deadline":
                return Comparator.comparing(TodoItem::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
            case "status":
                return Comparator.comparing(TodoItem::getStatus);
            case "createdate":
            default:
                return Comparator.comparing(TodoItem::getCreatedAt);
        }
    }
    
    private boolean wouldCreateCircularDependency(TodoItem item, TodoItem newDependency) {
        // Check if newDependency depends on item (directly or indirectly)
        return checkDependencyChain(newDependency, item.getId());
    }
    
    private boolean checkDependencyChain(TodoItem item, Long targetId) {
        if (item.getId().equals(targetId)) {
            return true;
        }
        
        for (TodoItem dependency : item.getDependencies()) {
            if (checkDependencyChain(dependency, targetId)) {
                return true;
            }
        }
        
        return false;
    }
}
