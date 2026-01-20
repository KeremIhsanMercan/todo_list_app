package com.example.todo_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.todo_app.model.TodoItem;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByTodoListId(Long todoListId);
    
    Optional<TodoItem> findByIdAndTodoListId(Long id, Long todoListId);
    
    @Query("SELECT t FROM TodoItem t WHERE t.todoList.id = :listId AND t.status = :status")
    List<TodoItem> findByTodoListIdAndStatus(@Param("listId") Long listId, @Param("status") String status);
    
    @Query("SELECT t FROM TodoItem t WHERE t.todoList.id = :listId AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<TodoItem> findByTodoListIdAndNameContaining(@Param("listId") Long listId, @Param("name") String name);
}
