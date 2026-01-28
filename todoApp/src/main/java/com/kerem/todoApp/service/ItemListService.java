package com.kerem.todoApp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kerem.todoApp.dto.ItemListCreateRequest;
import com.kerem.todoApp.dto.ItemListResponse;
import com.kerem.todoApp.dto.ItemListUpdateRequest;
import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.mapper.ItemListMapper;
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
    
    @Autowired
    private ItemListMapper itemListMapper;
    
    /**
     * Get all lists for a user
     */
    public List<ItemListResponse> getUserLists() {
        Long userId = SecurityUtils.getCurrentUserId();
        return itemListRepository.findByUserId(userId).stream()
                .map(itemListMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a single list by ID
     */
    public ItemListResponse getListById(Long listId) {
        Long userId = SecurityUtils.getCurrentUserId();
        com.kerem.todoApp.model.ItemList list = itemListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
        return itemListMapper.toResponse(list);
    }
    
    /**
     * Create a new list
     */
    public ItemListResponse createList(ItemListCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        com.kerem.todoApp.model.ItemList list = new com.kerem.todoApp.model.ItemList(request.getName(), user);
        com.kerem.todoApp.model.ItemList savedList = itemListRepository.save(list);
        return itemListMapper.toResponse(savedList);
    }
    
    /**
     * Update a list
     */
    public ItemListResponse updateList(Long listId, ItemListUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        com.kerem.todoApp.model.ItemList list = itemListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
        
        list.setName(request.getName());
        com.kerem.todoApp.model.ItemList updatedList = itemListRepository.save(list);
        return itemListMapper.toResponse(updatedList);
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
