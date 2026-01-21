// Test 2: TodoListTests.java - Unit tests for TodoList model
package com.example.todo_app.model;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TodoList Model Tests")
class TodoListTests {

    private TodoList todoList;
    private User user;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        todoList = new TodoList();
        todoList.setId(1L);
        todoList.setName("My Todo List");
        todoList.setUser(user);
    }

    @Test
    @DisplayName("Test 8: TodoList name should be set correctly")
    void testTodoListNameIsSetCorrectly() {
        assertEquals("My Todo List", todoList.getName());
    }

    @Test
    @DisplayName("Test 9: TodoList should be associated with correct user")
    void testTodoListUserAssociation() {
        assertEquals(user, todoList.getUser());
        assertEquals("testuser", todoList.getUser().getUsername());
    }

    @Test
    @DisplayName("Test 10: TodoList can have multiple items")
    void testTodoListCanHaveMultipleItems() {
        TodoItem item1 = new TodoItem();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setTodoList(todoList);

        TodoItem item2 = new TodoItem();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setTodoList(todoList);

        Set<TodoItem> items = new HashSet<>();
        items.add(item1);
        items.add(item2);
        todoList.setItems(items);

        assertEquals(2, todoList.getItems().size());
        assertTrue(todoList.getItems().contains(item1));
        assertTrue(todoList.getItems().contains(item2));
    }

    @Test
    @DisplayName("Test 11: New TodoList should have empty items list")
    void testNewTodoListHasEmptyItems() {
        TodoList newList = new TodoList();
        assertNotNull(newList.getItems());
        assertTrue(newList.getItems().isEmpty());
    }
}