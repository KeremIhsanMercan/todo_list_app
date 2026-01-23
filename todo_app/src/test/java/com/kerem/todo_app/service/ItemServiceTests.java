package com.kerem.todo_app.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kerem.todo_app.model.Item;
import com.kerem.todo_app.model.ItemStatus;
import com.kerem.todo_app.model.User;
import com.kerem.todo_app.repository.ItemRepository;
import com.kerem.todo_app.repository.ListRepository;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTests {
    
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ListRepository listRepository;
    
    @InjectMocks
    private ItemService itemService;
    
    private User testUser;
    private com.kerem.todo_app.model.List testList;
    private Item testItem1;
    private Item testItem2;
    private Item testItem3;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testList = new com.kerem.todo_app.model.List("Test List", testUser);
        testList.setId(1L);
        
        testItem1 = new Item("Item 1", "Description 1", LocalDate.now().plusDays(5), testList);
        testItem1.setId(1L);
        testItem1.setStatus(ItemStatus.NOT_STARTED);
        testItem1.setDependencies(new HashSet<>());
        
        testItem2 = new Item("Item 2", "Description 2", LocalDate.now().plusDays(10), testList);
        testItem2.setId(2L);
        testItem2.setStatus(ItemStatus.IN_PROGRESS);
        testItem2.setDependencies(new HashSet<>());
        
        testItem3 = new Item("Item 3", "Description 3", LocalDate.now().plusDays(3), testList);
        testItem3.setId(3L);
        testItem3.setStatus(ItemStatus.COMPLETED);
        testItem3.setDependencies(new HashSet<>());
    }
    
    @Test
    void testGetItemsForList_NoFilters() {
        // Arrange
        List<Item> items = Arrays.asList(testItem1, testItem2, testItem3);
        when(itemRepository.findByListId(1L)).thenReturn(items);
        
        // Act
        List<Item> result = itemService.getItemsForList(1L, null, null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(itemRepository).findByListId(1L);
    }
    
    @Test
    void testGetItemsForList_FilterByStatus() {
        // Arrange
        List<Item> items = Arrays.asList(testItem1, testItem2, testItem3);
        when(itemRepository.findByListId(1L)).thenReturn(items);
        
        // Act
        List<Item> result = itemService.getItemsForList(1L, "COMPLETED", null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ItemStatus.COMPLETED, result.get(0).getStatus());
    }
    
    @Test
    void testGetItemsForList_FilterByName() {
        // Arrange
        List<Item> items = Arrays.asList(testItem1, testItem2, testItem3);
        when(itemRepository.findByListId(1L)).thenReturn(items);
        
        // Act
        List<Item> result = itemService.getItemsForList(1L, null, "Item 1", null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item 1", result.get(0).getName());
    }
    
    @Test
    void testGetItemsForList_SortByName() {
        // Arrange
        List<Item> items = Arrays.asList(testItem3, testItem1, testItem2);
        when(itemRepository.findByListId(1L)).thenReturn(items);
        
        // Act
        List<Item> result = itemService.getItemsForList(1L, null, null, "name", "asc");
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Item 1", result.get(0).getName());
        assertEquals("Item 2", result.get(1).getName());
        assertEquals("Item 3", result.get(2).getName());
    }
    
    @Test
    void testGetItemById_Success() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act
        Item result = itemService.getItemById(1L, 1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("Item 1", result.getName());
        verify(listRepository).findByIdAndUserId(1L, 1L);
        verify(itemRepository).findByIdAndListId(1L, 1L);
    }
    
    @Test
    void testGetItemById_ListNotFound() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            itemService.getItemById(1L, 1L, 1L);
        });
        
        assertEquals("Todo list not found", exception.getMessage());
    }
    
    @Test
    void testGetItemById_ItemNotFound() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            itemService.getItemById(1L, 1L, 1L);
        });
        
        assertEquals("Item not found", exception.getMessage());
    }
    
    @Test
    void testCreateItem_Success() {
        // Arrange
        Item newItem = new Item("New Item", "New Description", LocalDate.now().plusDays(7), testList);
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.save(any(Item.class))).thenReturn(newItem);
        
        // Act
        Item result = itemService.createItem(1L, 1L, newItem);
        
        // Assert
        assertNotNull(result);
        verify(listRepository).findByIdAndUserId(1L, 1L);
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void testCreateItem_ListNotFound() {
        // Arrange
        Item newItem = new Item("New Item", "New Description", LocalDate.now().plusDays(7), testList);
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            itemService.createItem(1L, 1L, newItem);
        });
        
        assertEquals("Todo list not found", exception.getMessage());
    }
    
    @Test
    void testUpdateItem_Success() {
        // Arrange
        Item updatedDetails = new Item("Updated Item", "Updated Description", LocalDate.now().plusDays(10), testList);
        updatedDetails.setStatus(ItemStatus.IN_PROGRESS);
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        Item result = itemService.updateItem(1L, 1L, 1L, updatedDetails);
        
        // Assert
        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void testUpdateItem_CannotCompleteWithIncompleteDependencies() {
        // Arrange
        testItem1.getDependencies().add(testItem2); // testItem2 is IN_PROGRESS, not COMPLETED
        
        Item updatedDetails = new Item("Updated Item", "Updated Description", LocalDate.now().plusDays(10), testList);
        updatedDetails.setStatus(ItemStatus.COMPLETED);
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            itemService.updateItem(1L, 1L, 1L, updatedDetails);
        });
        
        assertEquals("Cannot mark as COMPLETED! Dependencies are not complete.", exception.getMessage());
    }
    
    @Test
    void testUpdateItem_CannotChangeFromCompletedWithCompletedDependents() {
        // Arrange
        testItem1.setStatus(ItemStatus.COMPLETED);
        testItem2.getDependencies().add(testItem1); // testItem2 depends on testItem1
        testItem2.setStatus(ItemStatus.COMPLETED);
        
        Item updatedDetails = new Item("Updated Item", "Updated Description", LocalDate.now().plusDays(10), testList);
        updatedDetails.setStatus(ItemStatus.IN_PROGRESS);
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByListId(1L)).thenReturn(Arrays.asList(testItem1, testItem2));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            itemService.updateItem(1L, 1L, 1L, updatedDetails);
        });
        
        assertEquals("Cannot change status! There are items depending on this that are COMPLETED.", exception.getMessage());
    }
    
    @Test
    void testMarkAsComplete_Success() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        Item result = itemService.markAsComplete(1L, 1L, 1L);
        
        // Assert
        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void testMarkAsComplete_DependenciesNotSatisfied() {
        // Arrange
        testItem1.getDependencies().add(testItem2); // testItem2 is IN_PROGRESS
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            itemService.markAsComplete(1L, 1L, 1L);
        });
        
        assertEquals("Cannot complete: Dependencies not satisfied.", exception.getMessage());
    }
    
    @Test
    void testAddDependency_Success() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByIdAndListId(2L, 1L)).thenReturn(Optional.of(testItem2));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        itemService.addDependency(1L, 1L, 2L, 1L);
        
        // Assert
        verify(itemRepository).save(testItem1);
    }
    
    @Test
    void testAddDependency_SelfDependency() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            itemService.addDependency(1L, 1L, 1L, 1L);
        });
        
        assertEquals("Item cannot depend on itself.", exception.getMessage());
    }
    
    @Test
    void testAddDependency_CircularDependency() {
        // Arrange
        testItem2.getDependencies().add(testItem1); // testItem2 depends on testItem1
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByIdAndListId(2L, 1L)).thenReturn(Optional.of(testItem2));
        
        // Act & Assert (trying to make testItem1 depend on testItem2 would create a circle)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            itemService.addDependency(1L, 1L, 2L, 1L);
        });
        
        assertEquals("This would create a circular dependency.", exception.getMessage());
    }
    
    @Test
    void testRemoveDependency_Success() {
        // Arrange
        testItem1.getDependencies().add(testItem2);
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByIdAndListId(2L, 1L)).thenReturn(Optional.of(testItem2));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        itemService.removeDependency(1L, 1L, 2L, 1L);
        
        // Assert
        verify(itemRepository).save(testItem1);
    }
    
    @Test
    void testDeleteItem_Success() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByListId(1L)).thenReturn(Arrays.asList(testItem1, testItem2));
        
        // Act
        itemService.deleteItem(1L, 1L, 1L);
        
        // Assert
        verify(itemRepository).delete(testItem1);
    }
    
    @Test
    void testDeleteItem_HasDependents() {
        // Arrange
        testItem2.getDependencies().add(testItem1); // testItem2 depends on testItem1
        
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByListId(1L)).thenReturn(Arrays.asList(testItem1, testItem2));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            itemService.deleteItem(1L, 1L, 1L);
        });
        
        assertEquals("Cannot delete: Other items depend on this item. Remove dependencies first.", exception.getMessage());
    }
}
