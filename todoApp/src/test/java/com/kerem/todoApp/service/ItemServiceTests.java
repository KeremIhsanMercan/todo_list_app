package com.kerem.todoApp.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.kerem.todoApp.dto.ItemCreateRequest;
import com.kerem.todoApp.dto.ItemResponse;
import com.kerem.todoApp.dto.ItemUpdateRequest;
import com.kerem.todoApp.exception.InvalidOperationException;
import com.kerem.todoApp.exception.ResourceNotFoundException;
import com.kerem.todoApp.mapper.ItemMapper;
import com.kerem.todoApp.model.Item;
import com.kerem.todoApp.model.ItemStatus;
import com.kerem.todoApp.model.User;
import com.kerem.todoApp.repository.ItemListRepository;
import com.kerem.todoApp.repository.ItemRepository;
import com.kerem.todoApp.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTests {
    
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ItemListRepository itemListRepository;
    
    @Mock
    private ItemMapper itemMapper;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private ItemService itemService;
    
    private User testUser;
    private com.kerem.todoApp.model.ItemList testList;
    private Item testItem1;
    private Item testItem2;
    private Item testItem3;
    private UserDetailsImpl userDetails;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testList = new com.kerem.todoApp.model.ItemList("Test List", testUser);
        testList.setId(1L);
        
        testItem1 = new Item("Item 1", "Description 1", LocalDate.now().plusDays(5), testList);
        testItem1.setId(1L);
        testItem1.setStatus(ItemStatus.NOT_STARTED);
        testItem1.setDependencies(new HashSet<>());
        testItem1.setDependents(new HashSet<>());
        
        testItem2 = new Item("Item 2", "Description 2", LocalDate.now().plusDays(10), testList);
        testItem2.setId(2L);
        testItem2.setStatus(ItemStatus.IN_PROGRESS);
        testItem2.setDependencies(new HashSet<>());
        testItem2.setDependents(new HashSet<>());
        
        testItem3 = new Item("Item 3", "Description 3", LocalDate.now().plusDays(3), testList);
        testItem3.setId(3L);
        testItem3.setStatus(ItemStatus.COMPLETED);
        testItem3.setDependencies(new HashSet<>());
        
        // Setup mock Authentication (lenient to avoid UnnecessaryStubbingException)
        userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password123");
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        
        // Setup mock ItemMapper (lenient to avoid UnnecessaryStubbingException)
        lenient().when(itemMapper.toResponse(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            ItemResponse response = new ItemResponse();
            response.setId(item.getId());
            response.setName(item.getName());
            response.setDescription(item.getDescription());
            response.setStatus(item.getStatus());
            response.setDeadline(item.getDeadline());
            response.setCreatedAt(item.getCreatedAt());
            response.setCompletedAt(item.getCompletedAt());
            response.setListId(item.getList() != null ? item.getList().getId() : null);
            // Map dependencies to DependencyInfo
            if (item.getDependencies() != null) {
                List<ItemResponse.DependencyInfo> deps = item.getDependencies().stream()
                    .map(dep -> {
                        ItemResponse.DependencyInfo info = new ItemResponse.DependencyInfo();
                        info.setId(dep.getId());
                        info.setName(dep.getName());
                        return info;
                    })
                    .collect(Collectors.toList());
                response.setDependencies(deps);
            }
            return response;
        });
        
        lenient().when(itemMapper.toEntity(any(ItemCreateRequest.class))).thenAnswer(invocation -> {
            ItemCreateRequest request = invocation.getArgument(0);
            Item item = new Item();
            item.setName(request.getName());
            item.setDescription(request.getDescription());
            item.setDeadline(request.getDeadline());
            item.setStatus(request.getStatus() != null ? request.getStatus() : ItemStatus.NOT_STARTED);
            return item;
        });
        
        lenient().doAnswer(invocation -> {
            ItemUpdateRequest request = invocation.getArgument(0);
            Item item = invocation.getArgument(1);
            item.setName(request.getName());
            item.setDescription(request.getDescription());
            item.setDeadline(request.getDeadline());
            if (request.getStatus() != null) {
                item.setStatus(request.getStatus());
            }
            return null;
        }).when(itemMapper).updateEntity(any(ItemUpdateRequest.class), any(Item.class));
    }
    
    @Test
    void testGetItemsForList_NoFilters() {
        // Arrange
        List<Item> items = Arrays.asList(testItem1, testItem2, testItem3);
        Page<Item> page = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByListIdWithFilters(any(Long.class), any(), any(), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<ItemResponse> result = itemService.getItemsForList(1L, null, null, pageable, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        verify(itemRepository).findByListIdWithFilters(any(Long.class), any(), any(), any(Pageable.class));
    }
    
    @Test
    void testGetItemsForList_FilterByStatus() {
        // Arrange
        List<Item> items = Arrays.asList(testItem3);
        Page<Item> page = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByListIdWithFilters(1L, ItemStatus.COMPLETED, null, pageable)).thenReturn(page);
        
        // Act
        Page<ItemResponse> result = itemService.getItemsForList(1L, ItemStatus.COMPLETED, null, pageable, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(ItemStatus.COMPLETED, result.getContent().get(0).getStatus());
    }
    
    @Test
    void testGetItemsForList_FilterByName() {
        // Arrange
        List<Item> items = Arrays.asList(testItem1);
        Page<Item> page = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByListIdWithFilters(1L, null, "Item 1", pageable)).thenReturn(page);
        
        // Act
        Page<ItemResponse> result = itemService.getItemsForList(1L, null, "Item 1", pageable, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Item 1", result.getContent().get(0).getName());
    }
    
    @Test
    void testGetItemsForList_SortByName() {
        // Arrange
        List<Item> items = Arrays.asList(testItem1, testItem2, testItem3);
        Page<Item> page = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("name").ascending());
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByListIdWithFilters(any(Long.class), any(), any(), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<ItemResponse> result = itemService.getItemsForList(1L, null, null, pageable, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("Item 1", result.getContent().get(0).getName());
        assertEquals("Item 2", result.getContent().get(1).getName());
        assertEquals("Item 3", result.getContent().get(2).getName());
    }
    
    @Test
    void testGetItemById_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act
        ItemResponse result = itemService.getItemById(1L, 1L, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals("Item 1", result.getName());
        verify(itemListRepository).findByIdAndUserId(1L, 1L);
        verify(itemRepository).findByIdAndListId(1L, 1L);
    }
    
    @Test
    void testGetItemById_ListNotFound() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.getItemById(1L, 1L, authentication);
        });
        
        assertEquals("Todo list not found", exception.getMessage());
    }
    
    @Test
    void testGetItemById_ItemNotFound() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.getItemById(1L, 1L, authentication);
        });
        
        assertEquals("Item not found", exception.getMessage());
    }
    
    @Test
    void testCreateItem_Success() {
        // Arrange
        ItemCreateRequest createRequest = new ItemCreateRequest();
        createRequest.setName("New Item");
        createRequest.setDescription("New Description");
        createRequest.setDeadline(LocalDate.now().plusDays(7));
        
        Item newItem = new Item("New Item", "New Description", LocalDate.now().plusDays(7), testList);
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.save(any(Item.class))).thenReturn(newItem);
        
        // Act
        ItemResponse result = itemService.createItem(1L, authentication, createRequest);
        
        // Assert
        assertNotNull(result);
        verify(itemListRepository).findByIdAndUserId(1L, 1L);
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void testCreateItem_ListNotFound() {
        // Arrange
        ItemCreateRequest createRequest = new ItemCreateRequest();
        createRequest.setName("New Item");
        createRequest.setDescription("New Description");
        createRequest.setDeadline(LocalDate.now().plusDays(7));
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.createItem(1L, authentication, createRequest);
        });
        
        assertEquals("Todo list not found", exception.getMessage());
    }
    
    @Test
    void testUpdateItem_Success() {
        // Arrange
        ItemUpdateRequest updateRequest = new ItemUpdateRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated Description");
        updateRequest.setDeadline(LocalDate.now().plusDays(10));
        updateRequest.setStatus(ItemStatus.IN_PROGRESS);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        ItemResponse result = itemService.updateItem(1L, 1L, authentication, updateRequest);
        
        // Assert
        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void testUpdateItem_CannotCompleteWithIncompleteDependencies() {
        // Arrange
        testItem1.getDependencies().add(testItem2); // testItem2 is IN_PROGRESS, not COMPLETED
        
        ItemUpdateRequest updateRequest = new ItemUpdateRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated Description");
        updateRequest.setDeadline(LocalDate.now().plusDays(10));
        updateRequest.setStatus(ItemStatus.COMPLETED);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(InvalidOperationException.class, () -> {
            itemService.updateItem(1L, 1L, authentication, updateRequest);
        });
        
        assertEquals("Cannot mark as COMPLETED! Dependencies are not complete.", exception.getMessage());
    }
    
    @Test
    void testUpdateItem_CannotChangeFromCompletedWithCompletedDependents() {
        // Arrange
        testItem1.setStatus(ItemStatus.COMPLETED);
        testItem2.getDependencies().add(testItem1); // testItem2 depends on testItem1
        testItem1.getDependents().add(testItem2); // testItem1 is depended on by testItem2
        testItem2.setStatus(ItemStatus.COMPLETED);
        
        ItemUpdateRequest updateRequest = new ItemUpdateRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated Description");
        updateRequest.setDeadline(LocalDate.now().plusDays(10));
        updateRequest.setStatus(ItemStatus.IN_PROGRESS);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(InvalidOperationException.class, () -> {
            itemService.updateItem(1L, 1L, authentication, updateRequest);
        });
        
        assertEquals("Cannot change status! There are items depending on this that are COMPLETED.", exception.getMessage());
    }
    
    @Test
    void testMarkAsComplete_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        ItemResponse result = itemService.markAsComplete(1L, 1L, authentication);
        
        // Assert
        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void testMarkAsComplete_DependenciesNotSatisfied() {
        // Arrange
        testItem1.getDependencies().add(testItem2); // testItem2 is IN_PROGRESS
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(InvalidOperationException.class, () -> {
            itemService.markAsComplete(1L, 1L, authentication);
        });
        
        assertEquals("Cannot complete: Dependencies not satisfied.", exception.getMessage());
    }
    
    @Test
    void testAddDependency_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByIdAndListId(2L, 1L)).thenReturn(Optional.of(testItem2));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        itemService.addDependency(1L, 1L, 2L, authentication);
        
        // Assert
        verify(itemRepository).save(testItem1);
    }
    
    @Test
    void testAddDependency_SelfDependency() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act & Assert
        Exception exception = assertThrows(InvalidOperationException.class, () -> {
            itemService.addDependency(1L, 1L, 1L, authentication);
        });
        
        assertEquals("Item cannot depend on itself.", exception.getMessage());
    }
    
    @Test
    void testAddDependency_CircularDependency() {
        // Arrange
        testItem2.getDependencies().add(testItem1); // testItem2 depends on testItem1
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByIdAndListId(2L, 1L)).thenReturn(Optional.of(testItem2));
        
        // Act & Assert (trying to make testItem1 depend on testItem2 would create a circle)
        Exception exception = assertThrows(InvalidOperationException.class, () -> {
            itemService.addDependency(1L, 1L, 2L, authentication);
        });
        
        assertEquals("This would create a circular dependency.", exception.getMessage());
    }
    
    @Test
    void testRemoveDependency_Success() {
        // Arrange
        testItem1.getDependencies().add(testItem2);
        
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.findByIdAndListId(2L, 1L)).thenReturn(Optional.of(testItem2));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem1);
        
        // Act
        itemService.removeDependency(1L, 1L, 2L, authentication);
        
        // Assert
        verify(itemRepository).save(testItem1);
    }
    
    @Test
    void testDeleteItem_Success() {
        // Arrange
        when(itemListRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testList));
        when(itemRepository.findByIdAndListId(1L, 1L)).thenReturn(Optional.of(testItem1));
        
        // Act
        itemService.deleteItem(1L, 1L, authentication);
        
        // Assert
        verify(itemRepository).delete(testItem1);
    }
}