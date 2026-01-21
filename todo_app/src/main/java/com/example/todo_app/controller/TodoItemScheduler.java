package com.example.todo_app.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.todo_app.model.TodoItem;
import com.example.todo_app.repository.TodoItemRepository;

@Service
public class TodoItemScheduler {
    
    @Autowired
    private TodoItemRepository todoItemRepository;
    
    // Run every hour (3600000 ms = 1 hour)
    @Scheduled(fixedRate = 3600000)
    public void updateExpiredItems() {
        LocalDateTime now = LocalDateTime.now();
        List<TodoItem> allItems = todoItemRepository.findAll();
        
        for (TodoItem item : allItems) {
            if (item.getDeadline() != null && 
                !item.getStatus().equals("COMPLETED") && 
                item.getDeadline().isBefore(now) &&
                !item.getStatus().equals("EXPIRED")) {
                
                item.setStatus("EXPIRED");
                todoItemRepository.save(item);
            }
        }
        
        System.out.println("Checked and updated expired items at: " + now);
    }
}
