package com.kerem.todo_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerem.todo_app.dto.MessageResponse;
import com.kerem.todo_app.model.List;
import com.kerem.todo_app.security.UserDetailsImpl;
import com.kerem.todo_app.service.ListService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class ListController {
    
    @Autowired
    private ListService listService;
    
    // Get all lists for current user
    @GetMapping
    public ResponseEntity<java.util.List<List>> getUserLists(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        java.util.List<List> lists = listService.getUserLists(userId);
        return ResponseEntity.ok(lists);
    }
    
    // Get list by id
    @GetMapping("/{id}")
    public ResponseEntity<List> getListById(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            List list = listService.getListById(id, userId);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Create new list
    @PostMapping
    public ResponseEntity<List> createList(@Valid @RequestBody List listRequest, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            List savedList = listService.createList(listRequest.getName(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Update list
    @PutMapping("/{id}")
    public ResponseEntity<List> updateList(@PathVariable Long id, 
                                           @Valid @RequestBody List listDetails,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            List updatedList = listService.updateList(id, listDetails.getName(), userId);
            return ResponseEntity.ok(updatedList);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Delete list
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteList(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        try {
            listService.deleteList(id, userId);
            return ResponseEntity.ok(new MessageResponse("List deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
