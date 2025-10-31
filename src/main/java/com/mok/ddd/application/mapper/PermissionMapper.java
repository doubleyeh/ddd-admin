package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.PermissionDTO;
import com.mok.ddd.domain.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionDTO toDto(Permission entity);

    Permission toEntity(PermissionDTO dto);
}