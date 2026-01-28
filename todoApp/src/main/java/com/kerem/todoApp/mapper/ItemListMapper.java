package com.kerem.todoApp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.kerem.todoApp.dto.ItemListCreateRequest;
import com.kerem.todoApp.dto.ItemListResponse;
import com.kerem.todoApp.dto.ItemListUpdateRequest;
import com.kerem.todoApp.model.ItemList;

@Mapper(componentModel = "spring")
public interface ItemListMapper {
    
    @Mapping(target = "itemCount", expression = "java(itemList.getItems() != null ? itemList.getItems().size() : 0)")
    ItemListResponse toResponse(ItemList itemList);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ItemList toEntity(ItemListCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ItemList toEntity(ItemListUpdateRequest request);
}
