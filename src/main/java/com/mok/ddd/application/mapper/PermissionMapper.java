package com.mok.ddd.application.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.domain.entity.Permission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionDTO toDto(Permission entity);

    Permission toEntity(PermissionDTO dto);

    default List<PermissionDTO> toDtoList(Collection<Permission> list) {
        if (list == null) {
            return Collections.emptyList();
        }

        return list.stream().map(this::toDto).toList();
    }
}