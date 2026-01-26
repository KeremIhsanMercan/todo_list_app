package com.kerem.todoApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.kerem.todoApp.Parameters;
import com.kerem.todoApp.dto.ItemCreateRequest;
import com.kerem.todoApp.dto.ItemResponse;
import com.kerem.todoApp.dto.ItemUpdateRequest;
import com.kerem.todoApp.exception.InvalidOperationException;
import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.mapper.ItemMapper;
import com.kerem.todoApp.model.Item;
import com.kerem.todoApp.model.ItemList;
import com.kerem.todoApp.model.ItemStatus;
import com.kerem.todoApp.repository.ItemListRepository;
import com.kerem.todoApp.repository.ItemRepository;
import com.kerem.todoApp.security.UserDetailsImpl;

@Service
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private ItemListRepository itemListRepository;
    
    @Autowired
    private ItemMapper itemMapper;  // MapStruct will generate this
    
    /**
     * Get all items for a list with optional filtering, sorting, and pagination
     */
    public Page<ItemResponse> getItemsForList(Long listId, ItemStatus status, String name, 
                                               Pageable pageable, Authentication authentication) {
        validateAndGetList(listId, authentication);
        String trimmedName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        
        Page<Item> items = itemRepository.findByListIdWithFilters(listId, status, trimmedName, pageable);
        return items.map(itemMapper::toResponse);  // Convert to DTO
    }
    
    /**
     * Get a single item by ID
     */
    public ItemResponse getItemById(Long listId, Long itemId, Authentication authentication) {
        // Return ItemResponse DTO
        Item item = validateAndGetItem(listId, itemId, authentication);
        return itemMapper.toResponse(item);
    }
    
    /**
     * Create a new item
     */
    public ItemResponse createItem(Long listId, Authentication authentication, ItemCreateRequest request) {
        ItemList list = validateAndGetList(listId, authentication);
        
        Item item = itemMapper.toEntity(request);
        item.setList(list);
        
        Item saved = itemRepository.save(item);
        return itemMapper.toResponse(saved);
    }
    
    /**
     * Update an existing item
     */
    public ItemResponse updateItem(Long listId, Long itemId, Authentication authentication, ItemUpdateRequest request) {
        validateAndGetList(listId, authentication);
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        
        // Rule 1: Cannot mark as COMPLETED if this item's dependencies are not complete
        if (ItemStatus.COMPLETED.equals(request.getStatus()) && !item.canBeCompleted()) {
            throw new InvalidOperationException("Cannot mark as COMPLETED! Dependencies are not complete.");
        }
        
        // Rule 2: Cannot change from COMPLETED to something else if items depending on this are COMPLETED
        if (!ItemStatus.COMPLETED.equals(request.getStatus()) && ItemStatus.COMPLETED.equals(item.getStatus())) {
            boolean hasCompletedDependents = item.getDependents().stream()
                    .anyMatch(dependent -> ItemStatus.COMPLETED.equals(dependent.getStatus()));
            
            if (hasCompletedDependents) {
                throw new InvalidOperationException("Cannot change status! There are items depending on this that are COMPLETED.");
            }
        }
        
        itemMapper.updateEntity(request, item);  // MapStruct updates the entity
        Item saved = itemRepository.save(item);
        return itemMapper.toResponse(saved);
    }
    
    /**
     * Mark an item as complete
     */
    public ItemResponse markAsComplete(Long listId, Long itemId, Authentication authentication) {
        Item item = validateAndGetItem(listId, itemId, authentication);
        
        if (!item.canBeCompleted()) {
            throw new InvalidOperationException("Cannot complete: Dependencies not satisfied.");
        }
        
        item.setStatus(ItemStatus.COMPLETED);
        Item saved = itemRepository.save(item);
        return itemMapper.toResponse(saved);
    }
    
    /**
     * Add a dependency to an item
     */
    public void addDependency(Long listId, Long itemId, Long dependencyId, Authentication authentication) {
        validateAndGetList(listId, authentication);
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found."));
        
        Item dependency = itemRepository.findByIdAndListId(dependencyId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependency item not found."));
        
        if (itemId.equals(dependencyId)) {
            throw new InvalidOperationException("Item cannot depend on itself.");
        }
        
        // Check for circular dependency
        if (wouldCreateCircularDependency(item, dependency)) {
            throw new InvalidOperationException("This would create a circular dependency.");
        }
        
        item.getDependencies().add(dependency);
        itemRepository.save(item);
    }
    
    /**
     * Remove a dependency from an item
     */
    public void removeDependency(Long listId, Long itemId, Long dependencyId, Authentication authentication) {
        validateAndGetList(listId, authentication);
        
        Item item = itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found."));
        
        Item dependency = itemRepository.findByIdAndListId(dependencyId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependency item not found."));
        
        item.getDependencies().remove(dependency);
        itemRepository.save(item);
    }
    
    /**
     * Delete an item
     */
    public void deleteItem(Long listId, Long itemId, Authentication authentication) {
        Item item = validateAndGetItem(listId, itemId, authentication);
        
        // Remove this item from all dependents' dependencies
        for (Item dependent : item.getDependents()) {
            dependent.getDependencies().remove(item);
            itemRepository.save(dependent);
        }
        
        itemRepository.delete(item);
    }
    
    /**
     * Check if adding a dependency would create a circular dependency
     */
    private boolean wouldCreateCircularDependency(Item item, Item newDependency) {
        return checkDependencyChain(newDependency, item.getId(), 0L);
    }
    
    /**
     * Recursively check dependency chain for circular references
     */
    private boolean checkDependencyChain(Item item, Long targetId, Long depth) {

        if (depth > Parameters.MAX_DEPENDENCY_DEPTH) {
            throw new InvalidOperationException("Dependency chain too deep or possible circular dependency.");
        }

        if (item.getId().equals(targetId)) {
            return true;
        }
        
        for (Item dependency : item.getDependencies()) {
            if (checkDependencyChain(dependency, targetId, depth + 1)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Extract user ID from authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
    
    /**
     * Validate user owns the list and return it
     */
    private com.kerem.todoApp.model.ItemList validateAndGetList(Long listId, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return itemListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo list not found"));
    }
    
    /**
     * Validate user owns the list and return the item
     */
    private Item validateAndGetItem(Long listId, Long itemId, Authentication authentication) {
        validateAndGetList(listId, authentication);
        return itemRepository.findByIdAndListId(itemId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }
}
