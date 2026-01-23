package com.kerem.todo_app.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kerem.todo_app.model.Item;
import com.kerem.todo_app.model.ItemStatus;
import com.kerem.todo_app.repository.ItemRepository;
import com.kerem.todo_app.repository.ListRepository;

@Service
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private ListRepository listRepository;
    
    /**
     * Get all items for a list with optional filtering and sorting
     */
    public List<Item> getItemsForList(Long listId, String status, String name, String sortBy, String sortOrder) {
        List<Item> items = itemRepository.findByListId(listId);
        
        // Filter by status
        if (status != null && !status.isEmpty()) {
            try {
                ItemStatus statusEnum = ItemStatus.valueOf(status);
                items = items.stream()
                        .filter(item -> statusEnum.equals(item.getStatus()))
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status value, return all items
            }
        }
        
        // Filter by name
        if (name != null && !name.isEmpty()) {
            items = items.stream()
                    .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // Sort
        if (sortBy != null) {
            Comparator<Item> comparator = getComparator(sortBy);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            items = items.stream().sorted(comparator).collect(Collectors.toList());
        }
        
        return items;
    }
    
    /**
     * Get a single item by ID
     */
    public Item getItemById(Long listId, Long itemId, Long userId) {
        // Verify user owns this list
        listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        return itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }
    
    /**
     * Create a new item
     */
    public Item createItem(Long listId, Long userId, Item itemRequest) {
        com.kerem.todo_app.model.List list = listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        Item item = new Item(
                itemRequest.getName(),
                itemRequest.getDescription(),
                itemRequest.getDeadline(),
                list
        );
        
        if (itemRequest.getStatus() != null) {
            item.setStatus(itemRequest.getStatus());
        }
        
        return itemRepository.save(item);
    }
    
    /**
     * Update an existing item
     */
    public Item updateItem(Long listId, Long itemId, Long userId, Item itemDetails) {
        // Verify user owns the list
        listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        // Rule 1: Cannot mark as COMPLETED if this item's dependencies are not complete
        if (ItemStatus.COMPLETED.equals(itemDetails.getStatus()) && !item.canBeCompleted()) {
            throw new IllegalStateException("Cannot mark as COMPLETED! Dependencies are not complete.");
        }
        
        // Rule 2: Cannot change from COMPLETED to something else if items depending on this are COMPLETED
        if (!ItemStatus.COMPLETED.equals(itemDetails.getStatus()) && ItemStatus.COMPLETED.equals(item.getStatus())) {
            // Find items that depend on this item (items that have this item in their dependencies)
            List<Item> itemsDependingOnThis = itemRepository.findByListId(listId).stream()
                    .filter(otherItem -> otherItem.getDependencies().stream()
                            .anyMatch(dep -> dep.getId().equals(itemId)))
                    .collect(Collectors.toList());
            
            // Check if any of those items are COMPLETED
            boolean hasCompletedDependents = itemsDependingOnThis.stream()
                    .anyMatch(dependent -> ItemStatus.COMPLETED.equals(dependent.getStatus()));
            
            if (hasCompletedDependents) {
                throw new IllegalStateException("Cannot change status! There are items depending on this that are COMPLETED.");
            }
        }
        
        // Update item fields
        item.setName(itemDetails.getName());
        item.setDescription(itemDetails.getDescription());
        item.setDeadline(itemDetails.getDeadline());
        item.setStatus(itemDetails.getStatus());
        
        if (ItemStatus.COMPLETED.equals(itemDetails.getStatus())) {
            item.setCompletedAt(LocalDateTime.now());
        }
        
        return itemRepository.save(item);
    }
    
    /**
     * Mark an item as complete
     */
    public Item markAsComplete(Long listId, Long itemId, Long userId) {
        listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        if (!item.canBeCompleted()) {
            throw new IllegalStateException("Cannot complete: Dependencies not satisfied.");
        }
        
        item.setStatus(ItemStatus.COMPLETED);
        item.setCompletedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }
    
    /**
     * Add a dependency to an item
     */
    public void addDependency(Long listId, Long itemId, Long dependencyId, Long userId) {
        listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found."));
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found."));
        
        Item dependency = itemRepository.findByIdAndListId(dependencyId, listId)
                .orElseThrow(() -> new RuntimeException("Dependency item not found."));
        
        if (itemId.equals(dependencyId)) {
            throw new IllegalArgumentException("Item cannot depend on itself.");
        }
        
        // Check for circular dependency
        if (wouldCreateCircularDependency(item, dependency)) {
            throw new IllegalStateException("This would create a circular dependency.");
        }
        
        item.getDependencies().add(dependency);
        itemRepository.save(item);
    }
    
    /**
     * Remove a dependency from an item
     */
    public void removeDependency(Long listId, Long itemId, Long dependencyId, Long userId) {
        listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found."));
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found."));
        
        Item dependency = itemRepository.findByIdAndListId(dependencyId, listId)
                .orElseThrow(() -> new RuntimeException("Dependency item not found."));
        
        item.getDependencies().remove(dependency);
        itemRepository.save(item);
    }
    
    /**
     * Delete an item
     */
    public void deleteItem(Long listId, Long itemId, Long userId) {
        listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("Todo list not found."));
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        // Check if other items depend on this item
        List<Item> itemsDependingOnThis = itemRepository.findByListId(listId).stream()
                .filter(otherItem -> otherItem.getDependencies().stream()
                        .anyMatch(dependency -> dependency.getId().equals(itemId)))
                .collect(Collectors.toList());
        
        if (!itemsDependingOnThis.isEmpty()) {
            throw new IllegalStateException("Cannot delete: Other items depend on this item. Remove dependencies first.");
        }
        
        itemRepository.delete(item);
    }
    
    /**
     * Get comparator for sorting items
     */
    private Comparator<Item> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(Item::getName);
            case "deadline":
                return Comparator.comparing(Item::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
            case "status":
                return Comparator.comparing(Item::getStatus);
            case "createdate":
            default:
                return Comparator.comparing(Item::getCreatedAt);
        }
    }
    
    /**
     * Check if adding a dependency would create a circular dependency
     */
    private boolean wouldCreateCircularDependency(Item item, Item newDependency) {
        return checkDependencyChain(newDependency, item.getId());
    }
    
    /**
     * Recursively check dependency chain for circular references
     */
    private boolean checkDependencyChain(Item item, Long targetId) {
        if (item.getId().equals(targetId)) {
            return true;
        }
        
        for (Item dependency : item.getDependencies()) {
            if (checkDependencyChain(dependency, targetId)) {
                return true;
            }
        }
        
        return false;
    }
}
