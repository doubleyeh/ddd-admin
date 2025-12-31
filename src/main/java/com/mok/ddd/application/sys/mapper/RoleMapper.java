package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.role.RoleDTO;
import com.mok.ddd.application.sys.dto.role.RoleOptionDTO;
import com.mok.ddd.application.sys.dto.role.RoleSaveDTO;
import com.mok.ddd.domain.sys.model.Role;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { PermissionMapper.class,
        MenuMapper.class })
public interface RoleMapper {

    RoleDTO toDto(Role entity);

    RoleOptionDTO toOptionsDto(Role entity);

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "menus", ignore = true)
    Role toEntity(RoleDTO dto);

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "menus", ignore = true)
    Role toEntity(RoleSaveDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "menus", ignore = true)
    void updateEntityFromDto(RoleSaveDTO dto, @MappingTarget Role entity);
}