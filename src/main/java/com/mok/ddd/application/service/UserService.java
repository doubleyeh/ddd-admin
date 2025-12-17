package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPasswordDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.dto.user.UserPutDTO;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.UserMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.*;
import com.mok.ddd.domain.repository.UserRepository;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;
    private final MenuMapper menuMapper;
    private final PermissionService permissionService;

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
        User entity = userMapper.postToEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    @TenantFilter(TenantFilter.TenantFilterPolicy.SKIP)
    public UserDTO createForTenant(@NonNull UserPostDTO dto, String tenantId) {
        if (userRepository.findByTenantIdAndUsername(tenantId, dto.getUsername()).isPresent()) {
            throw new BizException("用户名已存在");
        }
        User entity = userMapper.postToEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setTenantId(tenantId);

        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDTO updateUser(@NonNull UserPutDTO dto) {
        User entity = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));

        userMapper.putToEntity(dto, entity);
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDTO updateUserState(@NonNull Long id, @NonNull Integer state){
        User entity = userRepository.findById(id).orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));
        entity.setState(state);
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public void updatePassword(@NonNull UserPasswordDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
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
            flatMenus = new ArrayList<>(menuService.findAll());
            distinctPermissions = new HashSet<>(permissionService.getAllPermissionCodes());
        } else {
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
        return userMapper.toEntity(dto);
    }

    @Override
    protected UserDTO toDto(@NonNull User entity) {
        return userMapper.toDto(entity);
    }
}
