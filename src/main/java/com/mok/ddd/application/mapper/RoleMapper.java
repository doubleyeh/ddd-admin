package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.RoleDTO;
import com.mok.ddd.application.dto.RoleSaveDTO;
import com.mok.ddd.domain.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PermissionMapper.class, MenuMapper.class})
public interface RoleMapper {

    RoleDTO toDto(Role entity);

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "menus", ignore = true)
    Role toEntity(RoleDTO dto);

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "menus", ignore = true)
    Role toEntity(RoleSaveDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "menus", ignore = true)
    void updateEntityFromDto(RoleSaveDTO dto, @MappingTarget Role entity);
}