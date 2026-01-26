package com.kerem.todoApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kerem.todoApp.model.ItemList;

@Repository
public interface ItemListRepository extends JpaRepository<ItemList, Long> {
    java.util.List<ItemList> findByUserId(Long userId);
    Optional<ItemList> findByIdAndUserId(Long id, Long userId);
}
