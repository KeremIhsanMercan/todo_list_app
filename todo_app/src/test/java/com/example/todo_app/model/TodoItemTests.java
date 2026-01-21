// Test 1: TodoItemTests.java - Unit tests for TodoItem model
package com.example.todo_app.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TodoItem Model Tests")
class TodoItemTests {

    private TodoItem todoItem;
    private TodoList todoList;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        todoList = new TodoList();
        todoList.setId(1L);
        todoList.setName("Test List");

        todoItem = new TodoItem();
        todoItem.setId(1L);
        todoItem.setName("Test Item");
        todoItem.setDescription("Test Description");
        todoItem.setStatus("NOT_STARTED");
        todoItem.setTodoList(todoList);
    }

    @Test
    @DisplayName("Test 1: Item should be expired when deadline is in the past")
    void testItemIsExpired() {
        // Set deadline to yesterday
        todoItem.setDeadline(LocalDateTime.now().minusDays(1));
        todoItem.setStatus("NOT_STARTED");

        assertTrue(todoItem.isExpired(), "Item should be expired when deadline is past");
    }

    @Test
    @DisplayName("Test 2: Item should not be expired when deadline is in the future")
    void testItemIsNotExpired() {
        // Set deadline to tomorrow
        todoItem.setDeadline(LocalDateTime.now().plusDays(1));
        todoItem.setStatus("NOT_STARTED");

        assertFalse(todoItem.isExpired(), "Item should not be expired when deadline is in future");
    }

    @Test
    @DisplayName("Test 3: Completed item should never be expired")
    void testCompletedItemIsNotExpired() {
        // Set deadline to yesterday but mark as completed
        todoItem.setDeadline(LocalDateTime.now().minusDays(1));
        todoItem.setStatus("COMPLETED");

        assertFalse(todoItem.isExpired(), "Completed item should never be expired");
    }

    @Test
    @DisplayName("Test 4: Item can be completed when no dependencies exist")
    void testCanBeCompletedWithNoDependencies() {
        todoItem.setDependencies(new HashSet<>());

        assertTrue(todoItem.canBeCompleted(), "Item with no dependencies should be completable");
    }

    @Test
    @DisplayName("Test 5: Item cannot be completed when dependencies are not complete")
    void testCannotBeCompletedWithIncompleteDependencies() {
        TodoItem dependency = new TodoItem();
        dependency.setId(2L);
        dependency.setName("Dependency Item");
        dependency.setStatus("NOT_STARTED");

        Set<TodoItem> dependencies = new HashSet<>();
        dependencies.add(dependency);
        todoItem.setDependencies(dependencies);

        assertFalse(todoItem.canBeCompleted(), "Item should not be completable when dependencies are incomplete");
    }

    @Test
    @DisplayName("Test 6: Item can be completed when all dependencies are complete")
    void testCanBeCompletedWithCompletedDependencies() {
        TodoItem dependency1 = new TodoItem();
        dependency1.setId(2L);
        dependency1.setName("Dependency 1");
        dependency1.setStatus("COMPLETED");

        TodoItem dependency2 = new TodoItem();
        dependency2.setId(3L);
        dependency2.setName("Dependency 2");
        dependency2.setStatus("COMPLETED");

        Set<TodoItem> dependencies = new HashSet<>();
        dependencies.add(dependency1);
        dependencies.add(dependency2);
        todoItem.setDependencies(dependencies);

        assertTrue(todoItem.canBeCompleted(), "Item should be completable when all dependencies are completed");
    }

    @Test
    @DisplayName("Test 7: Item properties are correctly set and retrieved")
    void testItemPropertiesSetAndGet() {
        assertEquals("Test Item", todoItem.getName());
        assertEquals("Test Description", todoItem.getDescription());
        assertEquals("NOT_STARTED", todoItem.getStatus());
        assertEquals(1L, todoItem.getId());
        assertEquals(todoList, todoItem.getTodoList());
    }
}