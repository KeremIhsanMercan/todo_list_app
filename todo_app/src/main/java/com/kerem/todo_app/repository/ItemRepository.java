package com.kerem.todo_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kerem.todo_app.model.Item;
import com.kerem.todo_app.model.ItemStatus;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    java.util.List<Item> findByListId(Long listId);
    
    Optional<Item> findByIdAndListId(Long id, Long listId);
    
    @Query("SELECT t FROM Item t WHERE t.list.id = :listId AND t.status = :status")
    java.util.List<Item> findByListIdAndStatus(@Param("listId") Long listId, @Param("status") ItemStatus status);
    
    @Query("SELECT t FROM Item t WHERE t.list.id = :listId AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<Item> findByListIdAndNameContaining(@Param("listId") Long listId, @Param("name") String name);
}
