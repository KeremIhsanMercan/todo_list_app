package com.kerem.todoApp.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kerem.todoApp.model.Item;
import com.kerem.todoApp.model.ItemStatus;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    java.util.List<Item> findByListId(Long listId);
    
    Optional<Item> findByIdAndListId(Long id, Long listId);
    
    @Query("SELECT t FROM Item t WHERE t.list.id = :listId AND t.status = :status")
    java.util.List<Item> findByListIdAndStatus(@Param("listId") Long listId, @Param("status") ItemStatus status);
    
    @Query("SELECT t FROM Item t WHERE t.list.id = :listId AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<Item> findByListIdAndNameContaining(@Param("listId") Long listId, @Param("name") String name);
    
    @Query("SELECT t FROM Item t WHERE t.list.id = :listId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Item> findByListIdWithFilters(
        @Param("listId") Long listId,
        @Param("status") ItemStatus status,
        @Param("name") String name,
        Pageable pageable
    );
}

