package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.dto.tenantPackage.TenantPackageOptionDTO;
import com.mok.ddd.application.dto.tenantPackage.TenantPackageSaveDTO;
import com.mok.ddd.domain.entity.TenantPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {MenuMapper.class, PermissionMapper.class})
public interface TenantPackageMapper {

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    TenantPackageDTO toDto(TenantPackage entity);

    TenantPackage toEntity(TenantPackageSaveDTO dto);

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    TenantPackage toEntity(TenantPackageDTO dto);

    void updateEntityFromDto(TenantPackageSaveDTO dto, @MappingTarget TenantPackage entity);

    List<TenantPackageOptionDTO> dtoToOptionsDto(List<TenantPackageDTO> dtoList);
}
