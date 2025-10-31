package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.MenuDTO;
import com.mok.ddd.application.dto.PermissionDTO;
import com.mok.ddd.application.dto.RoleDTO;
import com.mok.ddd.application.dto.RoleSaveDTO;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.PermissionMapper;
import com.mok.ddd.application.mapper.RoleMapper;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.entity.Role;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.domain.repository.PermissionRepository;
import com.mok.ddd.domain.repository.RoleRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService extends BaseServiceImpl<Role, Long, RoleDTO> {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final MenuMapper menuMapper;

    @Override
    protected CustomRepository<Role, Long> getRepository() {
        return roleRepository;
    }

    @Override
    protected Role toEntity(RoleDTO dto) {
        return roleMapper.toEntity(dto);
    }

    @Override
    protected RoleDTO toDto(Role entity) {
        return roleMapper.toDto(entity);
    }


    @Transactional
    public RoleDTO createRole(RoleSaveDTO dto) {
        Role entity = roleMapper.toEntity(dto);

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
        Set<Menu> menus = new HashSet<>(menuRepository.findAllById(dto.getMenuIds()));

        entity.setPermissions(permissions);
        entity.setMenus(menus);

        return roleMapper.toDto(roleRepository.save(entity));
    }

    @Transactional
    public RoleDTO updateRole(RoleSaveDTO dto) {
        Role existingRole = roleRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);

        roleMapper.updateEntityFromDto(dto, existingRole);

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
        Set<Menu> menus = new HashSet<>(menuRepository.findAllById(dto.getMenuIds()));

        existingRole.setPermissions(permissions);
        existingRole.setMenus(menus);

        return roleMapper.toDto(roleRepository.save(existingRole));
    }

    @Transactional(readOnly = true)
    public Set<MenuDTO> getMenusByRole(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(NotFoundException::new);
        return role.getMenus().stream()
                .map(menuMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<PermissionDTO> getPermissionsByRole(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(NotFoundException::new);
        return role.getPermissions().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toSet());
    }
}