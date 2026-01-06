package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.role.RoleDTO;
import com.mok.ddd.application.sys.dto.role.RoleOptionDTO;
import com.mok.ddd.domain.sys.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { PermissionMapper.class,
        MenuMapper.class })
public interface RoleMapper {

    RoleDTO toDto(Role entity);

    RoleOptionDTO toOptionsDto(Role entity);

}
