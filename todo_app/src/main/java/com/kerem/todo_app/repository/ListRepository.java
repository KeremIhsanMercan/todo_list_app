package com.kerem.todo_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kerem.todo_app.model.List;

@Repository
public interface ListRepository extends JpaRepository<List, Long> {
    java.util.List<List> findByUserId(Long userId);
    Optional<List> findByIdAndUserId(Long id, Long userId);
}
