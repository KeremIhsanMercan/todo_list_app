package com.kerem.todoApp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kerem.todoApp.dto.ItemListCreateRequest;
import com.kerem.todoApp.dto.ItemListResponse;
import com.kerem.todoApp.dto.ItemListUpdateRequest;
import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.mapper.ItemListMapper;
import com.kerem.todoApp.model.User;
import com.kerem.todoApp.repository.ItemListRepository;
import com.kerem.todoApp.repository.UserRepository;
import com.kerem.todoApp.security.SecurityUtils;

@ExtendWith(MockitoExtension.class)
public class ItemListServiceTests {
    
    @Mock
    private ItemListRepository itemListRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ItemListMapper itemListMapper;
    
    @InjectMocks
    private ItemListService itemListService;
    
    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User testUser;
    private com.kerem.todoApp.model.ItemList testList1;
    private com.kerem.todoApp.model.ItemList testList2;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testList1 = new com.kerem.todoApp.model.ItemList("List 1", testUser);
        testList1.setId(1L);
        
        testList2 = new com.kerem.todoApp.model.ItemList("List 2", testUser);
        testList2.setId(2L);
        
        // Mock SecurityUtils to return user ID
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
    }
    
    @SuppressWarnings("unused")
    @AfterEach
    void tearDown() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }
    
    @Test
    void testGetUserLists_Success() {
        // Arrange
        List<com.kerem.todoApp.model.ItemList> lists = Arrays.asList(testList1, testList2);
        ItemListResponse response1 = new ItemListResponse();
        response1.setId(1L);
        response1.setName("List 1");
        ItemListResponse response2 = new ItemListResponse();
        response2.setId(2L);
        response2.setName("List 2");
        
        when(itemListRepository.findByUserId(1L)).thenReturn(lists);
        when(itemListMapper.toResponse(testList1)).thenReturn(response1);
        when(itemListMapper.toResponse(testList2)).thenReturn(response2);
        
        // Act
        List<ItemListResponse> result = itemListService.getUserLists();
        
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
        List<ItemListResponse> result = itemListService.getUserLists();
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void testGetListById_Success() {
        // Arrange
        ItemListResponse response = new ItemListResponse();
        response.setId(1L);
        response.setName("List 1");
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        when(itemListMapper.toResponse(testList1)).thenReturn(response);
        
        // Act
        ItemListResponse result = itemListService.getListById(1L);
        
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
            itemListService.getListById(1L);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
    
    @Test
    void testCreateList_Success() {
        // Arrange
        ItemListCreateRequest request = new ItemListCreateRequest();
        request.setName("New List");
        ItemListResponse response = new ItemListResponse();
        response.setId(1L);
        response.setName("New List");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemListRepository.save(any(com.kerem.todoApp.model.ItemList.class))).thenReturn(testList1);
        when(itemListMapper.toResponse(testList1)).thenReturn(response);
        
        // Act
        ItemListResponse result = itemListService.createList(request);
        
        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(itemListRepository).save(any(com.kerem.todoApp.model.ItemList.class));
    }
    
    @Test
    void testCreateList_UserNotFound() {
        // Arrange
        ItemListCreateRequest request = new ItemListCreateRequest();
        request.setName("New List");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemListService.createList(request);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void testUpdateList_Success() {
        // Arrange
        ItemListUpdateRequest request = new ItemListUpdateRequest();
        request.setName("Updated List");
        ItemListResponse response = new ItemListResponse();
        response.setId(1L);
        response.setName("Updated List");
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        when(itemListRepository.save(any(com.kerem.todoApp.model.ItemList.class))).thenReturn(testList1);
        when(itemListMapper.toResponse(testList1)).thenReturn(response);
        
        // Act
        ItemListResponse result = itemListService.updateList(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(itemListRepository).findByIdAndUserId(1L, 1L);
        verify(itemListRepository).save(testList1);
    }
    
    @Test
    void testUpdateList_NotFound() {
        // Arrange
        ItemListUpdateRequest request = new ItemListUpdateRequest();
        request.setName("Updated List");
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemListService.updateList(1L, request);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
    
    @Test
    void testDeleteList_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList1));
        
        // Act
        itemListService.deleteList(1L);
        
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
            itemListService.deleteList(1L);
        });
        
        assertEquals("List not found", exception.getMessage());
    }
}
