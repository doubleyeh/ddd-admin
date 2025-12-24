package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.dto.tenantPackage.TenantPackageOptionDTO;
import com.mok.ddd.application.dto.tenantPackage.TenantPackageSaveDTO;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.entity.TenantPackage;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantPackageMapper {

    @Mapping(target = "menuIds", source = "menus", qualifiedByName = "menusToIds")
    @Mapping(target = "permissionIds", source = "permissions", qualifiedByName = "permissionsToIds")
    TenantPackageDTO toDto(TenantPackage entity);

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    TenantPackage toEntity(TenantPackageSaveDTO dto);

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    TenantPackage toEntity(TenantPackageDTO dto);

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntityFromDto(TenantPackageSaveDTO dto, @MappingTarget TenantPackage entity);

    List<TenantPackageOptionDTO> dtoToOptionsDto(List<TenantPackageDTO> dtoList);

    @Named("menusToIds")
    default Set<Long> menusToIds(Set<Menu> menus) {
        if (menus == null) return null;
        return menus.stream().map(Menu::getId).collect(Collectors.toSet());
    }

    @Named("permissionsToIds")
    default Set<Long> permissionsToIds(Set<Permission> permissions) {
        if (permissions == null) return null;
        return permissions.stream().map(Permission::getId).collect(Collectors.toSet());
    }
}