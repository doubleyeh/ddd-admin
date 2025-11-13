package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.*;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.UserMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.tenant.SkipTenantFilter;
import lombok.AllArgsConstructor;
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

    @Transactional
    public UserDTO create(UserPostDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BizException("用户名已存在");
        }
        User entity = userMapper.postToEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    @SkipTenantFilter
    public UserDTO createForTenant(UserPostDTO dto, String tenantId) {
        if (userRepository.findByTenantIdAndUsername(tenantId, dto.getUsername()).isPresent()) {
            throw new BizException("用户名已存在");
        }
        User entity = userMapper.postToEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setTenantId(tenantId);

        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDTO updateUser(UserPutDTO dto) {
        User entity = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));

        userMapper.putToEntity(dto, entity);
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public void updatePassword(UserPasswordDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(Const.NOT_FOUND_MESSAGE));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<User> userToDelete = userRepository.findById(id);

        if(userToDelete.isEmpty()){
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

        if(SysUtil.isSuperAdmin(user.getTenantId(), username)){
            flatMenus = menuService.findAll();
            distinctPermissions = new HashSet<>(permissionService.getAllPermissionCodes());
        }else{
            Set<Menu> distinctMenuEntities = user.getRoles().stream()
                    .flatMap(role -> role.getMenus().stream())
                    .collect(Collectors.toSet());

            flatMenus = menuMapper.toDtoList(distinctMenuEntities);

            distinctPermissions = user.getRoles().stream()
                    .flatMap(r->r.getPermissions().stream())
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
    protected User toEntity(UserDTO dto) {
        return userMapper.toEntity(dto);
    }

    @Override
    protected UserDTO toDto(User entity) {
        return userMapper.toDto(entity);
    }
}
