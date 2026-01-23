package com.kerem.todo_app.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kerem.todo_app.model.User;
import com.kerem.todo_app.repository.ListRepository;
import com.kerem.todo_app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ListServiceTests {
    
    @Mock
    private ListRepository listRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ListService listService;
    
    private User testUser;
    private com.kerem.todo_app.model.List testList1;
    private com.kerem.todo_app.model.List testList2;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testList1 = new com.kerem.todo_app.model.List("List 1", testUser);
        testList1.setId(1L);
        
        testList2 = new com.kerem.todo_app.model.List("List 2", testUser);
        testList2.setId(2L);
    }
    
    @Test
    void testGetUserLists_Success() {
        // Arrange
        List<com.kerem.todo_app.model.List> lists = Arrays.asList(testList1, testList2);
        when(listRepository.findByUserId(1L)).thenReturn(lists);
        
        // Act
        List<com.kerem.todo_app.model.List> result = listService.getUserLists(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("List 1", result.get(0).getName());
        assertEquals("List 2", result.get(1).getName());
        verify(listRepository).findByUserId(1L);
    }
    
    @Test
    void testGetUserLists_EmptyList() {
        // Arrange
        when(listRepository.findByUserId(1L)).thenReturn(Arrays.asList());
        
        // Act
        List<com.kerem.todo_app.model.List> result = listService.getUserLists(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void testGetListById_Success() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        
        // Act
        com.kerem.todo_app.model.List result = listService.getListById(1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("List 1", result.getName());
        assertEquals(1L, result.getId());
        verify(listRepository).findByIdAndUserId(1L, 1L);
    }
    
    @Test
    void testGetListById_NotFound() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            listService.getListById(1L, 1L);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
    
    @Test
    void testCreateList_Success() {
        // Arrange
        String listName = "New List";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(listRepository.save(any(com.kerem.todo_app.model.List.class))).thenReturn(testList1);
        
        // Act
        com.kerem.todo_app.model.List result = listService.createList(listName, 1L);
        
        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(listRepository).save(any(com.kerem.todo_app.model.List.class));
    }
    
    @Test
    void testCreateList_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            listService.createList("New List", 1L);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void testUpdateList_Success() {
        // Arrange
        String newName = "Updated List";
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        when(listRepository.save(any(com.kerem.todo_app.model.List.class))).thenReturn(testList1);
        
        // Act
        com.kerem.todo_app.model.List result = listService.updateList(1L, newName, 1L);
        
        // Assert
        assertNotNull(result);
        verify(listRepository).findByIdAndUserId(1L, 1L);
        verify(listRepository).save(testList1);
    }
    
    @Test
    void testUpdateList_NotFound() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            listService.updateList(1L, "Updated List", 1L);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
    
    @Test
    void testDeleteList_Success() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        
        // Act
        listService.deleteList(1L, 1L);
        
        // Assert
        verify(listRepository).findByIdAndUserId(1L, 1L);
        verify(listRepository).delete(testList1);
    }
    
    @Test
    void testDeleteList_NotFound() {
        // Arrange
        when(listRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            listService.deleteList(1L, 1L);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
}
