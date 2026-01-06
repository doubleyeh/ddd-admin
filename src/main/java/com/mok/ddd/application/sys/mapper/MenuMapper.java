package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper {

    @Mapping(target = "permissionIds", source = "permissions", qualifiedByName = "permsToIds")
    @Mapping(source = "parent.id", target = "parentId")
    MenuDTO toDto(Menu entity);

    default List<MenuDTO> toDtoList(Collection<Menu> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toDto).toList();
    }

    @Named("permsToIds")
    default Set<Long> permsToIds(Set<Permission> permissions) {
        if (permissions == null) {
            return Collections.emptySet();
        }
        return permissions.stream().map(Permission::getId).collect(Collectors.toSet());
    }
}
