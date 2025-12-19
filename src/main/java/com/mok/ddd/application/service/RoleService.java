package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.dto.role.*;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.PermissionMapper;
import com.mok.ddd.application.mapper.RoleMapper;
import com.mok.ddd.domain.entity.*;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.domain.repository.PermissionRepository;
import com.mok.ddd.domain.repository.RoleRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    protected Role toEntity(@NonNull RoleDTO dto) {
        return roleMapper.toEntity(dto);
    }

    @Override
    protected RoleDTO toDto(@NonNull Role entity) {
        return roleMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull RoleDTO> findPage(Predicate predicate, Pageable pageable){
        QRole role = QRole.role;
        QTenant tenant = QTenant.tenant;

        JPAQuery<RoleDTO> query = roleRepository.getJPAQueryFactory()
                .select(Projections.bean(RoleDTO.class,
                        role.id,
                        role.name,
                        role.code,
                        role.description,
                        role.sort,
                        role.enabled,
                        role.tenantId,
                        tenant.name.as("tenantName")
                ))
                .from(role)
                .leftJoin(tenant).on(role.tenantId.eq(tenant.tenantId))
                .where(predicate);

        roleRepository.applyTenantFilter(query, role);
        JPQLQuery<RoleDTO> paginatedQuery = roleRepository.getQuerydsl().applyPagination(pageable, query);

        return PageableExecutionUtils.getPage(paginatedQuery.fetch(), pageable, () -> {
            JPAQuery<Long> countQuery = roleRepository.getJPAQueryFactory()
                    .select(role.count())
                    .from(role)
                    .leftJoin(tenant).on(role.tenantId.eq(tenant.tenantId))
                    .where(predicate);
            return Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        });
    }

    @Transactional
    public RoleDTO createRole(@NonNull RoleSaveDTO dto) {
        Role entity = roleMapper.toEntity(dto);

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
        Set<Menu> menus = new HashSet<>(menuRepository.findAllById(dto.getMenuIds()));

        entity.setPermissions(permissions);
        entity.setMenus(menus);

        return roleMapper.toDto(roleRepository.save(entity));
    }

    @Transactional
    public RoleDTO updateRole(@NonNull RoleSaveDTO dto) {
        Role existingRole = roleRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);

        roleMapper.updateEntityFromDto(dto, existingRole);

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
        Set<Menu> menus = new HashSet<>(menuRepository.findAllById(dto.getMenuIds()));

        existingRole.setPermissions(permissions);
        existingRole.setMenus(menus);

        return roleMapper.toDto(roleRepository.save(existingRole));
    }

    @Transactional
    public RoleDTO updateState(Long id, Boolean enabled) {
        Role role = roleRepository.findById(id).orElseThrow(NotFoundException::new);
        role.setEnabled(enabled);
        return this.toDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRoleBeforeValidation(Long id) {
        if (roleRepository.existsUserAssociatedWithRole(id)) {
            throw new BizException("该角色下存在用户，请先删除用户关联该角色");
        }
        roleRepository.deleteById(id);
    }

    /**
     * 授权
     */
    @Transactional
    public void grant(Long roleId, RoleGrantDTO dto) {
        Role role = roleRepository.findById(roleId).orElseThrow(NotFoundException::new);

        if (dto.getMenuIds() != null) {
            role.setMenus(new HashSet<>(menuRepository.findAllById(dto.getMenuIds())));
        }

        if (dto.getPermissionIds() != null) {
            role.setPermissions(new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds())));
        }

        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<RoleOptionsDTO> getRoleOptions(RoleQuery roleQuery) {
        QRole role = QRole.role;
        QTenant tenant = QTenant.tenant;

        JPAQuery<RoleOptionsDTO> query = roleRepository.getJPAQueryFactory()
                .select(Projections.bean(RoleOptionsDTO.class,
                        role.id,
                        role.name,
                        tenant.name.as("tenantName")
                ))
                .from(role)
                .leftJoin(tenant).on(role.tenantId.eq(tenant.tenantId))
                .where(roleQuery.toPredicate());

        roleRepository.applyTenantFilter(query, role);
        return query.fetch();
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