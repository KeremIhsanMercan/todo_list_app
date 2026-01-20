package com.example.todo_app.controller;

import com.example.todo_app.dto.MessageResponse;
import com.example.todo_app.model.TodoList;
import com.example.todo_app.model.User;
import com.example.todo_app.repository.TodoListRepository;
import com.example.todo_app.repository.UserRepository;
import com.example.todo_app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todolists")
@CrossOrigin(origins = "*")
public class TodoListController {
    
    @Autowired
    private TodoListRepository todoListRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Get all todo lists for current user
    @GetMapping
    public ResponseEntity<List<TodoList>> getUserTodoLists(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TodoList> todoLists = todoListRepository.findByUserId(userId);
        return ResponseEntity.ok(todoLists);
    }
    
    // Get todo list by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getTodoListById(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return todoListRepository.findByIdAndUserId(id, userId)
                .map(todoList -> ResponseEntity.ok(todoList))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Create new todo list
    @PostMapping
    public ResponseEntity<?> createTodoList(@Valid @RequestBody TodoList todoListRequest, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TodoList todoList = new TodoList(todoListRequest.getName(), user);
        TodoList savedList = todoListRepository.save(todoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedList);
    }
    
    // Update todo list
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodoList(@PathVariable Long id, 
                                           @Valid @RequestBody TodoList todoListDetails,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return todoListRepository.findByIdAndUserId(id, userId)
                .map(todoList -> {
                    todoList.setName(todoListDetails.getName());
                    TodoList updatedList = todoListRepository.save(todoList);
                    return ResponseEntity.ok(updatedList);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Delete todo list
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodoList(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return todoListRepository.findByIdAndUserId(id, userId)
                .map(todoList -> {
                    todoListRepository.delete(todoList);
                    return ResponseEntity.ok(new MessageResponse("Todo list deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
