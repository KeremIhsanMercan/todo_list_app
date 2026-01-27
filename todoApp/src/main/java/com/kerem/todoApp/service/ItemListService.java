package com.kerem.todoApp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.model.User;
import com.kerem.todoApp.repository.ItemListRepository;
import com.kerem.todoApp.repository.UserRepository;
import com.kerem.todoApp.security.SecurityUtils;

@Service
public class ItemListService {
    
    @Autowired
    private ItemListRepository itemListRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all lists for a user
     */
    public List<com.kerem.todoApp.model.ItemList> getUserLists() {
        Long userId = SecurityUtils.getCurrentUserId();
        return itemListRepository.findByUserId(userId);
    }
    
    /**
     * Get a single list by ID
     */
    public com.kerem.todoApp.model.ItemList getListById(Long listId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return itemListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
    }
    
    /**
     * Create a new list
     */
    public com.kerem.todoApp.model.ItemList createList(String listName) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        com.kerem.todoApp.model.ItemList list = new com.kerem.todoApp.model.ItemList(listName, user);
        return itemListRepository.save(list);
    }
    
    /**
     * Update a list
     */
    public com.kerem.todoApp.model.ItemList updateList(Long listId, String newName) {
        Long userId = SecurityUtils.getCurrentUserId();
        com.kerem.todoApp.model.ItemList list = itemListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
        
        list.setName(newName);
        return itemListRepository.save(list);
    }
    
    /**
     * Delete a list
     */
    public void deleteList(Long listId) {
        Long userId = SecurityUtils.getCurrentUserId();
        com.kerem.todoApp.model.ItemList list = itemListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
        
        itemListRepository.delete(list);
    }
}
