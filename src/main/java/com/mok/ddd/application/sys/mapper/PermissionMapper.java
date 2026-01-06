package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import com.mok.ddd.domain.sys.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    @Mapping(source = "menu.id", target = "menuId")
    PermissionDTO toDto(Permission entity);

    default List<PermissionDTO> toDtoList(Collection<Permission> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toDto).toList();
    }
}
