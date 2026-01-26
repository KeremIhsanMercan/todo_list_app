package com.kerem.todoApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.kerem.todoApp.dto.MessageResponse;
import com.kerem.todoApp.dto.ItemCreateRequest;
import com.kerem.todoApp.dto.ItemResponse;
import com.kerem.todoApp.dto.ItemUpdateRequest;
import com.kerem.todoApp.model.ItemStatus;
import com.kerem.todoApp.service.ItemService;
import com.kerem.todoApp.Parameters;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lists/{listId}/items")
@CrossOrigin(origins = "*")
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    // Get all items in a todo list with filtering, sorting, and pagination
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getTodoItems(
            @PathVariable Long listId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean expired,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = Parameters.DEFAULT_PAGE_SIZE_STR) int size,
            Authentication authentication) {
        
        // Parse status enum
        ItemStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = ItemStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, will be treated as null (no filter)
            }
        }
        
        // Map frontend sort field names to entity field names
        String entitySortField = mapSortField(sortBy);
        
        // Build Sort object
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, entitySortField);
        
        // Build Pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ItemResponse> items = itemService.getItemsForList(listId, statusEnum, name, pageable, authentication);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Map frontend sort field names to entity field names
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null) return "createdAt";
        
        return switch (sortBy.toLowerCase()) {
            case "createdate" -> "createdAt";
            case "name" -> "name";
            case "deadline" -> "deadline";
            case "status" -> "status";
            default -> "createdAt";
        };
    }
    
    // Get item by id
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponse> getTodoItem(@PathVariable Long listId, @PathVariable Long itemId, Authentication authentication) {
        ItemResponse item = itemService.getItemById(listId, itemId, authentication);
        return ResponseEntity.ok(item);
    }

    // Create new todo item
    @PostMapping
    public ResponseEntity<ItemResponse> createTodoItem(@PathVariable Long listId,
                                       @Valid @RequestBody ItemCreateRequest itemRequest,
                                       Authentication authentication) {
        ItemResponse savedItem = itemService.createItem(listId, authentication, itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    // Update todo item
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponse> updateTodoItem(@PathVariable Long listId,
                                       @PathVariable Long itemId,
                                       @Valid @RequestBody ItemUpdateRequest itemDetails,
                                       Authentication authentication) {
        ItemResponse updatedItem = itemService.updateItem(listId, itemId, authentication, itemDetails);
        return ResponseEntity.ok(updatedItem);
    }
        
    // Mark item as complete
    @PatchMapping("/{itemId}/complete")
    public ResponseEntity<ItemResponse> markAsComplete(@PathVariable Long listId,
                                       @PathVariable Long itemId,
                                       Authentication authentication) {
        ItemResponse updatedItem = itemService.markAsComplete(listId, itemId, authentication);
        return ResponseEntity.ok(updatedItem);
    }
    
    // Add dependency
    @PostMapping("/{itemId}/dependencies/{dependencyId}")
    public ResponseEntity<MessageResponse> addDependency(@PathVariable Long listId,
                                          @PathVariable Long itemId,
                                          @PathVariable Long dependencyId,
                                          Authentication authentication) {
        itemService.addDependency(listId, itemId, dependencyId, authentication);
        return ResponseEntity.ok(new MessageResponse("Dependency added successfully."));
    }
    
    // Remove dependency
    @DeleteMapping("/{itemId}/dependencies/{dependencyId}")
    public ResponseEntity<MessageResponse> removeDependency(@PathVariable Long listId,
                                             @PathVariable Long itemId,
                                             @PathVariable Long dependencyId,
                                             Authentication authentication) {
        itemService.removeDependency(listId, itemId, dependencyId, authentication);
        return ResponseEntity.ok(new MessageResponse("Dependency removed successfully."));
    }
    
    // Delete todo item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<MessageResponse> deleteTodoItem(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           Authentication authentication) {
        itemService.deleteItem(listId, itemId, authentication);
        return ResponseEntity.ok(new MessageResponse("Todo item deleted successfully."));
    }
}

