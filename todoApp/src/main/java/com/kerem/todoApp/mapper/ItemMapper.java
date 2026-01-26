package com.kerem.todoApp.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.kerem.todoApp.dto.ItemCreateRequest;
import com.kerem.todoApp.dto.ItemResponse;
import com.kerem.todoApp.dto.ItemUpdateRequest;
import com.kerem.todoApp.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    
    @Mapping(target = "listId", source = "list.id")
    @Mapping(target = "dependencies", expression = "java(mapDependencies(item))")
    @Mapping(target = "expired", expression = "java(item.isExpired())")
    @Mapping(target = "canBeCompleted", expression = "java(item.canBeCompleted())")
    ItemResponse toResponse(Item item);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "dependents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Item toEntity(ItemCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "dependents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    void updateEntity(ItemUpdateRequest request, @MappingTarget Item item);
    
    default List<ItemResponse.DependencyInfo> mapDependencies(Item item) {
        return item.getDependencies().stream()
            .map(dep -> {
                ItemResponse.DependencyInfo info = new ItemResponse.DependencyInfo();
                info.setId(dep.getId());
                info.setName(dep.getName());
                return info;
            })
            .collect(Collectors.toList());
    }
}
