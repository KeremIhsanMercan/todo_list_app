package com.kerem.todo_app.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Item Model Tests")
class ItemTests {

    private Item item;
    private List list;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        list = new List();
        list.setId(1L);
        list.setName("Test List");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setStatus(ItemStatus.NOT_STARTED);
        item.setList(list);
    }

    @Test
    @DisplayName("Test 1: Item should be expired when deadline is in the past")
    void testItemIsExpired() {
        // Set deadline to yesterday
        item.setDeadline(LocalDate.now().minusDays(1));
        item.setStatus(ItemStatus.NOT_STARTED);

        assertTrue(item.isExpired(), "Item should be expired when deadline is past");
    }

    @Test
    @DisplayName("Test 2: Item should not be expired when deadline is in the future")
    void testItemIsNotExpired() {
        // Set deadline to tomorrow
        item.setDeadline(LocalDate.now().plusDays(1));
        item.setStatus(ItemStatus.NOT_STARTED);

        assertFalse(item.isExpired(), "Item should not be expired when deadline is in future");
    }

    @Test
    @DisplayName("Test 3: Completed item should never be expired")
    void testCompletedItemIsNotExpired() {
        // Set deadline to yesterday but mark as completed
        item.setDeadline(LocalDate.now().minusDays(1));
        item.setStatus(ItemStatus.COMPLETED);

        assertFalse(item.isExpired(), "Completed item should never be expired");
    }

    @Test
    @DisplayName("Test 4: Item can be completed when no dependencies exist")
    void testCanBeCompletedWithNoDependencies() {
        item.setDependencies(new HashSet<>());

        assertTrue(item.canBeCompleted(), "Item with no dependencies should be completable");
    }

    @Test
    @DisplayName("Test 5: Item cannot be completed when dependencies are not complete")
    void testCannotBeCompletedWithIncompleteDependencies() {
        Item dependency = new Item();
        dependency.setId(2L);
        dependency.setName("Dependency Item");
        dependency.setStatus(ItemStatus.NOT_STARTED);

        Set<Item> dependencies = new HashSet<>();
        dependencies.add(dependency);
        item.setDependencies(dependencies);

        assertFalse(item.canBeCompleted(), "Item should not be completable when dependencies are incomplete");
    }

    @Test
    @DisplayName("Test 6: Item can be completed when all dependencies are complete")
    void testCanBeCompletedWithCompletedDependencies() {
        Item dependency1 = new Item();
        dependency1.setId(2L);
        dependency1.setName("Dependency 1");
        dependency1.setStatus(ItemStatus.COMPLETED);

        Item dependency2 = new Item();
        dependency2.setId(3L);
        dependency2.setName("Dependency 2");
        dependency2.setStatus(ItemStatus.COMPLETED);

        Set<Item> dependencies = new HashSet<>();
        dependencies.add(dependency1);
        dependencies.add(dependency2);
        item.setDependencies(dependencies);

        assertTrue(item.canBeCompleted(), "Item should be completable when all dependencies are completed");
    }

    @Test
    @DisplayName("Test 7: Item properties are correctly set and retrieved")
    void testItemPropertiesSetAndGet() {
        assertEquals("Test Item", item.getName());
        assertEquals("Test Description", item.getDescription());
        assertEquals(ItemStatus.NOT_STARTED, item.getStatus());
        assertEquals(1L, item.getId());
        assertEquals(list, item.getList());
    }
}
