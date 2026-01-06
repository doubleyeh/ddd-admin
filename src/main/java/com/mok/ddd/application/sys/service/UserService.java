package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.sys.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.user.UserDTO;
import com.mok.ddd.application.sys.dto.user.UserPasswordDTO;
import com.mok.ddd.application.sys.dto.user.UserPostDTO;
import com.mok.ddd.application.sys.dto.user.UserPutDTO;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.application.sys.mapper.UserMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.*;
import com.mok.ddd.domain.sys.repository.RoleRepository;
import com.mok.ddd.domain.sys.repository.TenantPackageRepository;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.mok.ddd.domain.sys.repository.UserRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.infrastructure.tenant.TenantFilter;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService extends BaseServiceImpl<User, Long, UserDTO> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;
    private final MenuMapper menuMapper;
    private final PermissionService permissionService;
    private final TenantRepository tenantRepository;
    private final TenantPackageRepository tenantPackageRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<UserDTO> findPage(Predicate predicate, Pageable pageable){
        QUser user = QUser.user;
        QTenant tenant = QTenant.tenant;

        JPAQuery<UserDTO> query = userRepository.getJPAQueryFactory()
                .select(Projections.bean(UserDTO.class,
                        user.id,
                        user.username,
                        user.nickname,
                        user.state,
                        user.createTime,
                        user.tenantId,
                        tenant.name.as("tenantName")
                ))
                .from(user)
                .leftJoin(tenant).on(user.tenantId.eq(tenant.tenantId))
                .where(predicate);

        userRepository.applyTenantFilter(query, user);
        JPQLQuery<UserDTO> paginatedQuery = userRepository.getQuerydsl().applyPagination(pageable, query);

        return PageableExecutionUtils.getPage(paginatedQuery.fetch(), pageable, () -> {
            JPAQuery<Long> countQuery = userRepository.getJPAQueryFactory()
                    .select(user.count())
                    .from(user)
                    .leftJoin(tenant).on(user.tenantId.eq(tenant.tenantId))
                    .where(predicate);
            return Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        });
    }

    @Transactional
    public UserDTO create(@NonNull UserPostDTO dto) {
        if(!SysUtil.isSuperTenant(TenantContextHolder.getTenantId()) && !Objects.equals(dto.getTenantId(), TenantContextHolder.getTenantId())){
            throw new BizException("无权限管理其他用户");
        }
        if (userRepository.findByTenantIdAndUsername(dto.getTenantId(), dto.getUsername()).isPresent()) {
            throw new BizException("用户名已存在");
        }
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User entity = User.create(dto.getUsername(), encodedPassword, dto.getNickname(), false);

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            entity.changeRoles(roles);
        }

        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    @TenantFilter(TenantFilter.TenantFilterPolicy.SKIP)
    public UserDTO createForTenant(@NonNull UserPostDTO dto) {
        String tenantId = dto.getTenantId();
        if (tenantId == null) {
            throw new BizException("创建租户用户时，租户ID不能为空");
        }
        if (userRepository.findByTenantIdAndUsername(tenantId, dto.getUsername()).isPresent()) {
            throw new BizException("用户名已存在");
        }
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User entity = User.create(dto.getUsername(), encodedPassword, dto.getNickname(), true);
        entity.assignTenant(tenantId);

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            entity.changeRoles(roles);
        }

        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDTO updateUser(@NonNull UserPutDTO dto) {
        User entity = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));

        Set<Role> roles = null;
        if (dto.getRoleIds() != null) {
            roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
        }
        entity.updateInfo(dto.getNickname(), roles);
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDTO updateUserState(@NonNull Long id, @NonNull Integer state){
        User entity = userRepository.findById(id).orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));
        if (Objects.equals(state, Const.UserState.NORMAL)) {
            entity.enable();
        } else {
            entity.disable();
        }
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public void updatePassword(@NonNull UserPasswordDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.changePassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(@NonNull Long id) {
        Optional<User> userToDelete = userRepository.findById(id);

        if (userToDelete.isEmpty()) {
            throw new NotFoundException();
        }
        if (SysUtil.isSuperAdmin(userToDelete.get().getTenantId(), userToDelete.get().getUsername())) {
            throw new BizException("用户不允许删除");
        }
        if (Boolean.TRUE.equals(userToDelete.get().getIsTenantAdmin())) {
            throw new BizException("租户管理员不允许删除");
        }

        super.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        return this.toDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE)));
    }

    @Transactional(readOnly = true)
    public AccountInfoDTO findAccountInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));

        List<MenuDTO> flatMenus;
        Set<String> distinctPermissions;

        if (SysUtil.isSuperAdmin(user.getTenantId(), username)) {
            // 超级管理员拥有所有权限
            flatMenus = new ArrayList<>(menuService.findAll());
            distinctPermissions = new HashSet<>(permissionService.getAllPermissionCodes());
            distinctPermissions.add(Const.SUPER_ADMIN_ROLE_CODE);
        } else if (Boolean.TRUE.equals(user.getIsTenantAdmin())) {
            // 租户管理员，拥有该租户套餐下的所有权限
            Long packageId = tenantRepository.findByTenantId(user.getTenantId())
                    .map(Tenant::getPackageId)
                    .orElse(null);

            if (packageId != null) {
                TenantPackage tenantPackage = tenantPackageRepository.findById(packageId).orElse(null);
                if (tenantPackage != null) {
                    flatMenus = new ArrayList<>(menuMapper.toDtoList(tenantPackage.getMenus()));
                    distinctPermissions = tenantPackage.getPermissions().stream()
                            .map(Permission::getCode)
                            .collect(Collectors.toSet());
                } else {
                    flatMenus = new ArrayList<>();
                    distinctPermissions = new HashSet<>();
                }
            } else {
                flatMenus = new ArrayList<>();
                distinctPermissions = new HashSet<>();
            }
        } else {
            // 普通用户，根据角色获取权限
            Set<Menu> distinctMenuEntities = user.getRoles().stream()
                    .flatMap(role -> role.getMenus().stream())
                    .collect(Collectors.toSet());

            flatMenus = new ArrayList<>(menuMapper.toDtoList(distinctMenuEntities));

            distinctPermissions = user.getRoles().stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(Permission::getCode)
                    .collect(Collectors.toSet());
        }

        flatMenus.sort(Comparator.comparing(MenuDTO::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        List<MenuDTO> menuTree = menuService.buildMenuTree(flatMenus);

        return AccountInfoDTO.builder()
                .user(this.toDto(user))
                .menus(menuTree)
                .permissions(distinctPermissions).build();
    }

    @Override
    protected CustomRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected User toEntity(@NonNull UserDTO dto) {
        throw new UnsupportedOperationException("不支持从DTO创建或更新实体。");
    }

    @Override
    protected UserDTO toDto(@NonNull User entity) {
        return userMapper.toDto(entity);
    }
}
