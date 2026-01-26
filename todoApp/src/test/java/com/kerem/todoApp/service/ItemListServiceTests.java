package com.kerem.todoApp.service;

import java.util.Arrays;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.model.User;
import com.kerem.todoApp.repository.ItemListRepository;
import com.kerem.todoApp.repository.UserRepository;
import com.kerem.todoApp.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class ItemListServiceTests {
    
    @Mock
    private ItemListRepository itemListRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private ItemListService itemListService;
    
    private User testUser;
    private com.kerem.todoApp.model.ItemList testList1;
    private com.kerem.todoApp.model.ItemList testList2;
    private UserDetailsImpl userDetails;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testList1 = new com.kerem.todoApp.model.ItemList("List 1", testUser);
        testList1.setId(1L);
        
        testList2 = new com.kerem.todoApp.model.ItemList("List 2", testUser);
        testList2.setId(2L);
        
        // Setup mock Authentication (lenient to avoid UnnecessaryStubbingException)
        userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password123");
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    }
    
    @Test
    void testGetUserLists_Success() {
        // Arrange
        List<com.kerem.todoApp.model.ItemList> lists = Arrays.asList(testList1, testList2);
        when(itemListRepository.findByUserId(1L)).thenReturn(lists);
        
        // Act
        List<com.kerem.todoApp.model.ItemList> result = itemListService.getUserLists(authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("List 1", result.get(0).getName());
        assertEquals("List 2", result.get(1).getName());
        verify(itemListRepository).findByUserId(1L);
    }
    
    @Test
    void testGetUserLists_EmptyList() {
        // Arrange
        when(itemListRepository.findByUserId(1L)).thenReturn(Arrays.asList());
        
        // Act
        List<com.kerem.todoApp.model.ItemList> result = itemListService.getUserLists(authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void testGetListById_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        
        // Act
        com.kerem.todoApp.model.ItemList result = itemListService.getListById(1L, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals("List 1", result.getName());
        assertEquals(1L, result.getId());
        verify(itemListRepository).findByIdAndUserId(1L, 1L);
    }
    
    @Test
    void testGetListById_NotFound() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemListService.getListById(1L, authentication);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
    
    @Test
    void testCreateList_Success() {
        // Arrange
        String listName = "New List";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemListRepository.save(any(com.kerem.todoApp.model.ItemList.class))).thenReturn(testList1);
        
        // Act
        com.kerem.todoApp.model.ItemList result = itemListService.createList(listName, authentication);
        
        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(itemListRepository).save(any(com.kerem.todoApp.model.ItemList.class));
    }
    
    @Test
    void testCreateList_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemListService.createList("New List", authentication);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void testUpdateList_Success() {
        // Arrange
        String newName = "Updated List";
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        when(itemListRepository.save(any(com.kerem.todoApp.model.ItemList.class))).thenReturn(testList1);
        
        // Act
        com.kerem.todoApp.model.ItemList result = itemListService.updateList(1L, newName, authentication);
        
        // Assert
        assertNotNull(result);
        verify(itemListRepository).findByIdAndUserId(1L, 1L);
        verify(itemListRepository).save(testList1);
    }
    
    @Test
    void testUpdateList_NotFound() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemListService.updateList(1L, "Updated List", authentication);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
    
    @Test
    void testDeleteList_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        
        // Act
        itemListService.deleteList(1L, authentication);
        
        // Assert
        verify(itemListRepository).findByIdAndUserId(1L, 1L);
        verify(itemListRepository).delete(testList1);
    }
    
    @Test
    void testDeleteList_NotFound() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemListService.deleteList(1L, authentication);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
}
