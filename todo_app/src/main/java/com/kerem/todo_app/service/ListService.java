package com.kerem.todo_app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kerem.todo_app.model.User;
import com.kerem.todo_app.repository.ListRepository;
import com.kerem.todo_app.repository.UserRepository;

@Service
public class ListService {
    
    @Autowired
    private ListRepository listRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all lists for a user
     */
    public List<com.kerem.todo_app.model.List> getUserLists(Long userId) {
        return listRepository.findByUserId(userId);
    }
    
    /**
     * Get a single list by ID
     */
    public com.kerem.todo_app.model.List getListById(Long listId, Long userId) {
        return listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("List not found"));
    }
    
    /**
     * Create a new list
     */
    public com.kerem.todo_app.model.List createList(String listName, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        com.kerem.todo_app.model.List list = new com.kerem.todo_app.model.List(listName, user);
        return listRepository.save(list);
    }
    
    /**
     * Update a list
     */
    public com.kerem.todo_app.model.List updateList(Long listId, String newName, Long userId) {
        com.kerem.todo_app.model.List list = listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        
        list.setName(newName);
        return listRepository.save(list);
    }
    
    /**
     * Delete a list
     */
    public void deleteList(Long listId, Long userId) {
        com.kerem.todo_app.model.List list = listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        
        listRepository.delete(list);
    }
}
