package com.example.todo_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todo_app.model.TodoList;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long> {
    List<TodoList> findByUserId(Long userId);
    Optional<TodoList> findByIdAndUserId(Long id, Long userId);
}
