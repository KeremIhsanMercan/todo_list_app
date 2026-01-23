// Test 3: UserTests.java - Unit tests for User model
package com.kerem.todo_app.model;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Model Tests")
class UserTests {

    private User user;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword123");
    }

    @Test
    @DisplayName("Test 12: User properties should be set correctly")
    void testUserPropertiesAreSetCorrectly() {
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPassword());
    }

    @Test
    @DisplayName("Test 13: User can have multiple todo lists")
    void testUserCanHaveMultipleTodoLists() {
        List list1 = new List();
        list1.setId(1L);
        list1.setName("Work Tasks");
        list1.setUser(user);

        List list2 = new List();
        list2.setId(2L);
        list2.setName("Personal Tasks");
        list2.setUser(user);

        Set<List> lists = new HashSet<>();
        lists.add(list1);
        lists.add(list2);
        user.setLists(lists);

        assertEquals(2, user.getLists().size());
        assertTrue(user.getLists().contains(list1));
        assertTrue(user.getLists().contains(list2));
    }

    @Test
    @DisplayName("Test 14: User validation - username should not be null or empty")
    void testUsernameValidation() {
        assertNotNull(user.getUsername());
        assertFalse(user.getUsername().isEmpty());
    }

    @Test
    @DisplayName("Test 15: User validation - email should not be null or empty")
    void testEmailValidation() {
        assertNotNull(user.getEmail());
        assertFalse(user.getEmail().isEmpty());
        assertTrue(user.getEmail().contains("@"));
    }

    @Test
    @DisplayName("Test 16: New user should have empty todo lists")
    void testNewUserHasEmptyTodoLists() {
        User newUser = new User();
        assertNotNull(newUser.getLists());
        assertTrue(newUser.getLists().isEmpty());
    }
}
