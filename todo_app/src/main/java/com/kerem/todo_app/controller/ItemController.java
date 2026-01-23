package com.kerem.todo_app.controller;

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

import com.kerem.todo_app.dto.MessageResponse;
import com.kerem.todo_app.model.Item;
import com.kerem.todo_app.security.UserDetailsImpl;
import com.kerem.todo_app.service.ItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lists/{listId}/items")
@CrossOrigin(origins = "*")
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    // Get all items in a todo list with filtering and sorting
    @GetMapping
    public ResponseEntity<java.util.List<Item>> getTodoItems(
            @PathVariable Long listId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean expired,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            Authentication authentication) {
        
        java.util.List<Item> items = itemService.getItemsForList(listId, status, name, sortBy, sortOrder);
        return ResponseEntity.ok(items);
    }
    
    // Get item by id
    @GetMapping("/{itemId}")
    public ResponseEntity<Item> getTodoItem(@PathVariable Long listId, @PathVariable Long itemId, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            Item item = itemService.getItemById(listId, itemId, userId);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Create new todo item
    @PostMapping
    public ResponseEntity<Item> createTodoItem(@PathVariable Long listId,
                                           @Valid @RequestBody Item itemRequest,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            Item savedItem = itemService.createItem(listId, userId, itemRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Update todo item
    @PutMapping("/{itemId}")
    public ResponseEntity<Object> updateTodoItem(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           @Valid @RequestBody Item itemDetails,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            Item updatedItem = itemService.updateItem(listId, itemId, userId, itemDetails);
            return ResponseEntity.ok((Object) updatedItem);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body((Object) new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Mark item as complete
    @PatchMapping("/{itemId}/complete")
    public ResponseEntity<Object> markAsComplete(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            Item updatedItem = itemService.markAsComplete(listId, itemId, userId);
            return ResponseEntity.ok((Object) updatedItem);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body((Object) new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Add dependency
    @PostMapping("/{itemId}/dependencies/{dependencyId}")
    public ResponseEntity<MessageResponse> addDependency(@PathVariable Long listId,
                                          @PathVariable Long itemId,
                                          @PathVariable Long dependencyId,
                                          Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            itemService.addDependency(listId, itemId, dependencyId, userId);
            return ResponseEntity.ok(new MessageResponse("Dependency added successfully."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    // Remove dependency
    @DeleteMapping("/{itemId}/dependencies/{dependencyId}")
    public ResponseEntity<MessageResponse> removeDependency(@PathVariable Long listId,
                                             @PathVariable Long itemId,
                                             @PathVariable Long dependencyId,
                                             Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            itemService.removeDependency(listId, itemId, dependencyId, userId);
            return ResponseEntity.ok(new MessageResponse("Dependency removed successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    // Delete todo item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<MessageResponse> deleteTodoItem(@PathVariable Long listId,
                                           @PathVariable Long itemId,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            itemService.deleteItem(listId, itemId, userId);
            return ResponseEntity.ok(new MessageResponse("Todo item deleted successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
