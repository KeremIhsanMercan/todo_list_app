package com.kerem.todoApp.controller;

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

import com.kerem.todoApp.dto.MessageResponse;
import com.kerem.todoApp.model.ItemList;
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
    public ResponseEntity<java.util.List<ItemList>> getUserLists(Authentication authentication) {
        java.util.List<ItemList> lists = itemListService.getUserLists(authentication);
        return ResponseEntity.ok(lists);
    }
    
    // Get list by id
    @GetMapping("/{id}")
    public ResponseEntity<ItemList> getListById(@PathVariable Long id, Authentication authentication) {
        ItemList list = itemListService.getListById(id, authentication);
        return ResponseEntity.ok(list);
    }
    
    // Create new list
    @PostMapping
    public ResponseEntity<ItemList> createList(@Valid @RequestBody ItemList listRequest, Authentication authentication) {
        ItemList savedList = itemListService.createList(listRequest.getName(), authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedList);
    }
    
    // Update list
    @PutMapping("/{id}")
    public ResponseEntity<ItemList> updateList(@PathVariable Long id, 
                                           @Valid @RequestBody ItemList listDetails,
                                           Authentication authentication) {
        ItemList updatedList = itemListService.updateList(id, listDetails.getName(), authentication);
        return ResponseEntity.ok(updatedList);
    }
    
    // Delete list
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteList(@PathVariable Long id, Authentication authentication) {
        itemListService.deleteList(id, authentication);
        return ResponseEntity.ok(new MessageResponse("List deleted successfully"));
    }
}
