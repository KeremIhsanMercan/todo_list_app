package com.kerem.todoApp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kerem.todoApp.model.Item;
import com.kerem.todoApp.model.ItemStatus;
import com.kerem.todoApp.repository.ItemRepository;

@Service
public class ItemScheduler {
    
    @Autowired
    private ItemRepository itemRepository;
    
    // Run every hour (3600000 ms = 1 hour)
    @Scheduled(fixedRate = 3600000)
    public void updateExpiredItems() {
        LocalDate now = LocalDate.now();
        List<Item> allItems = itemRepository.findAll();
        
        for (Item item : allItems) {
            if (item.getDeadline() != null && 
                !item.getStatus().equals(ItemStatus.COMPLETED) && 
                item.getDeadline().isBefore(now) &&
                !item.getStatus().equals(ItemStatus.EXPIRED)) {
                
                item.setStatus(ItemStatus.EXPIRED);
                itemRepository.save(item);
            }
        }
        
        System.out.println("Checked and updated expired items at: " + now);
    }
}

