// Test 2: ListTests.java - Unit tests for List model
package com.kerem.todo_app.model;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("List Model Tests")
class ListTests {

    private List list;
    private User user;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        list = new List();
        list.setId(1L);
        list.setName("My Todo List");
        list.setUser(user);
    }

    @Test
    @DisplayName("Test 8: List name should be set correctly")
    void testTodoListNameIsSetCorrectly() {
        assertEquals("My Todo List", list.getName());
    }

    @Test
    @DisplayName("Test 9: List should be associated with correct user")
    void testTodoListUserAssociation() {
        assertEquals(user, list.getUser());
        assertEquals("testuser", list.getUser().getUsername());
    }

    @Test
    @DisplayName("Test 10: List can have multiple items")
    void testTodoListCanHaveMultipleItems() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setList(list);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setList(list);

        Set<Item> items = new HashSet<>();
        items.add(item1);
        items.add(item2);
        list.setItems(items);

        assertEquals(2, list.getItems().size());
        assertTrue(list.getItems().contains(item1));
        assertTrue(list.getItems().contains(item2));
    }

    @Test
    @DisplayName("Test 11: New List should have empty items list")
    void testNewTodoListHasEmptyItems() {
        List newList = new List();
        assertNotNull(newList.getItems());
        assertTrue(newList.getItems().isEmpty());
    }
}
