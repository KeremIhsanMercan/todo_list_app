package com.kerem.todoApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerem.todoApp.dto.ItemListCreateRequest;
import com.kerem.todoApp.dto.ItemListResponse;
import com.kerem.todoApp.dto.ItemListUpdateRequest;
import com.kerem.todoApp.dto.MessageResponse;
import com.kerem.todoApp.service.ItemListService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class ItemListController {
    
    @Autowired
    private ItemListService itemListService;
    
    // Get all lists for current user
    @GetMapping
    public ResponseEntity<java.util.List<ItemListResponse>> getUserLists() {
        java.util.List<ItemListResponse> lists = itemListService.getUserLists();
        return ResponseEntity.ok(lists);
    }
    
    // Get list by id
    @GetMapping("/{id}")
    public ResponseEntity<ItemListResponse> getListById(@PathVariable Long id) {
        ItemListResponse list = itemListService.getListById(id);
        return ResponseEntity.ok(list);
    }
    
    // Create new list
    @PostMapping
    public ResponseEntity<ItemListResponse> createList(@Valid @RequestBody ItemListCreateRequest request) {
        ItemListResponse savedList = itemListService.createList(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedList);
    }
    
    // Update list
    @PutMapping("/{id}")
    public ResponseEntity<ItemListResponse> updateList(@PathVariable Long id, 
                                           @Valid @RequestBody ItemListUpdateRequest request) {
        ItemListResponse updatedList = itemListService.updateList(id, request);
        return ResponseEntity.ok(updatedList);
    }
    
    // Delete list
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteList(@PathVariable Long id) {
        itemListService.deleteList(id);
        return ResponseEntity.ok(new MessageResponse("List deleted successfully"));
    }
}
