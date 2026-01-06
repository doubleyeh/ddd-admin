package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageOptionDTO;
import com.mok.ddd.domain.sys.model.TenantPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {MenuMapper.class, PermissionMapper.class})
public interface TenantPackageMapper {

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    TenantPackageDTO toDto(TenantPackage entity);

    List<TenantPackageOptionDTO> dtoToOptionsDto(List<TenantPackageDTO> dtoList);
}
