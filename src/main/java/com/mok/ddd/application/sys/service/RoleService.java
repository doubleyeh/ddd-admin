package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import com.mok.ddd.application.sys.dto.role.*;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.application.sys.mapper.PermissionMapper;
import com.mok.ddd.application.sys.mapper.RoleMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.QRole;
import com.mok.ddd.domain.sys.model.QTenant;
import com.mok.ddd.domain.sys.model.Role;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.domain.sys.repository.RoleRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.util.QuerydslUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

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

        Pageable qSortPageable = QuerydslUtils.convertToQSortPageable(pageable, Role.class, "role");

        JPAQuery<RoleDTO> query = roleRepository.getJPAQueryFactory()
                .select(Projections.bean(RoleDTO.class,
                        role.id,
                        role.name,
                        role.code,
                        role.description,
                        role.sort,
                        role.state,
                        role.tenantId,
                        tenant.name.as("tenantName"),
                        role.createTime.as("createTime")
                ))
                .from(role)
                .leftJoin(tenant).on(role.tenantId.eq(tenant.tenantId))
                .where(predicate);

        roleRepository.applyTenantFilter(query, role);
        JPQLQuery<RoleDTO> paginatedQuery = roleRepository.getQuerydsl().applyPagination(qSortPageable, query);

        return PageableExecutionUtils.getPage(paginatedQuery.fetch(), pageable, () -> {
            JPAQuery<Long> countQuery = roleRepository.getJPAQueryFactory()
                    .select(role.count())
                    .from(role)
                    .leftJoin(tenant).on(role.tenantId.eq(tenant.tenantId))
                    .where(predicate);
            return Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public RoleDTO getById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(NotFoundException::new);
        RoleDTO dto = roleMapper.toDto(role);

        dto.setMenus(role.getMenus().stream()
                        .map(menuMapper::toDto)
                .collect(Collectors.toSet()));

        dto.setPermissions(role.getPermissions().stream()
                        .map(permissionMapper::toDto)
                .collect(Collectors.toSet()));
        return dto;
    }

    @Transactional
    public RoleDTO createRole(@NonNull RoleSaveDTO dto) {
        Role entity = roleMapper.toEntity(dto);
        if (entity.getState() == null) {
            entity.setState(Const.RoleState.NORMAL);
        }
        return roleMapper.toDto(roleRepository.save(entity));
    }

    @Transactional
    public RoleDTO updateRole(@NonNull RoleSaveDTO dto) {
        Role existingRole = roleRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
        roleMapper.updateEntityFromDto(dto, existingRole);
        return roleMapper.toDto(roleRepository.save(existingRole));
    }

    @Transactional
    public RoleDTO updateState(Long id, Integer state) {
        Role role = roleRepository.findById(id).orElseThrow(NotFoundException::new);
        role.setState(state);
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

        String cacheKey = Const.CacheKey.ROLE_PERMS + ":" + roleId;
        redisTemplate.delete(cacheKey);
    }

    @Transactional(readOnly = true)
    public List<RoleOptionDTO> getRoleOptions(RoleQuery roleQuery) {
        QRole role = QRole.role;
        QTenant tenant = QTenant.tenant;

        JPAQuery<RoleOptionDTO> query = roleRepository.getJPAQueryFactory()
                .select(Projections.bean(RoleOptionDTO.class,
                        role.id,
                        role.name,
                        tenant.name.as("tenantName")
                ))
                .from(role)
                .leftJoin(tenant).on(role.tenantId.eq(tenant.tenantId))
                .where(roleQuery.toPredicate(),
                        role.state.eq(Const.RoleState.NORMAL));

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
