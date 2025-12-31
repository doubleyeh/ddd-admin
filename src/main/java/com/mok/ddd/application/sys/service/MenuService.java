package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.menu.MenuOptionDTO;
import com.mok.ddd.application.sys.dto.permission.PermissionOptionDTO;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService extends BaseServiceImpl<Menu, Long, MenuDTO> {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final StringRedisTemplate redisTemplate;
    private final MenuMapper menuMapper;
    private final TenantRepository tenantRepository;
    private final TenantPackageService tenantPackageService;

    @Transactional
    @Override
    public MenuDTO save(@NonNull MenuDTO dto) {
        Menu menu;
        if (dto.getId() != null) {
            menu = menuRepository.findById(dto.getId()).orElse(menuMapper.toEntity(dto));
            menuMapper.updateEntityFromDto(dto, menu);
        } else {
            menu = menuMapper.toEntity(dto);
        }

        if (dto.getPermissionIds() != null) {
            List<Permission> permissions = permissionRepository.findAllById(dto.getPermissionIds());

            if (menu.getPermissions() != null) {
                menu.getPermissions().forEach(p -> p.setMenu(null));
                menu.getPermissions().clear();
            } else {
                menu.setPermissions(new HashSet<>());
            }

            permissions.forEach(p -> {
                p.setMenu(menu);
                menu.getPermissions().add(p);
            });
        }

        Menu savedMenu = menuRepository.save(menu);
        return menuMapper.toDto(savedMenu);
    }

    public List<MenuDTO> buildMenuTree(@NonNull List<MenuDTO> flatList) {
        Map<Long, MenuDTO> dtoMap = flatList.stream()
                .collect(Collectors.toMap(MenuDTO::getId, dto -> dto));

        List<MenuDTO> rootMenus = new ArrayList<>();

        for (MenuDTO dto : flatList) {
            if (Boolean.TRUE.equals(dto.getIsHidden())) {
                continue;
            }

            if (dto.getParentId() == null || dto.getParentId() == 0) {
                rootMenus.add(dto);
            } else {
                MenuDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }

        return filterEmptyParentMenus(rootMenus);
    }

    @Transactional
    public void deleteById(Long id) {
        List<Long> allIds = getAllMenuIds(id);

        List<Long> roleIds = menuRepository.findRoleIdsByMenuIds(allIds);

        permissionRepository.deleteRolePermissionsByMenuIds(allIds);
        permissionRepository.deleteByMenuIds(allIds);

        menuRepository.deleteRoleMenuByMenuIds(allIds);
        menuRepository.deleteAllById(allIds);

        redisTemplate.delete(Const.CacheKey.MENU_TREE);
        if (!roleIds.isEmpty()) {
            List<String> keys = roleIds.stream()
                    .map(roleId -> Const.CacheKey.ROLE_PERMS + ":" + roleId)
                    .toList();
            redisTemplate.delete(keys);
        }
    }

    private List<Long> getAllMenuIds(Long parentId) {
        List<Long> ids = new ArrayList<>();
        ids.add(parentId);
        List<Menu> children = menuRepository.findByParentId(parentId);
        for (Menu child : children) {
            ids.addAll(getAllMenuIds(child.getId()));
        }
        return ids;
    }

    private List<MenuDTO> filterEmptyParentMenus(@NonNull List<MenuDTO> menus) {
        List<MenuDTO> filtered = new ArrayList<>();
        for (MenuDTO menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                List<MenuDTO> filteredChildren = filterEmptyParentMenus(menu.getChildren());
                menu.setChildren(filteredChildren);
            }

            if ((menu.getChildren() == null || menu.getChildren().isEmpty()) && menu.getPath() != null
                    && !menu.getPath().isEmpty()) {
                filtered.add(menu);
            } else if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                filtered.add(menu);
            }
        }
        return filtered;
    }

    @Transactional(readOnly = true)
    public List<MenuOptionDTO> buildMenuAndPermissionTree() {
        List<Menu> entities = menuRepository.findAll();

        // 如果不是超级租户，则只能看到套餐内的菜单和权限
        String currentTenantId = TenantContextHolder.getTenantId();
        if (!SysUtil.isSuperTenant(currentTenantId)) {
            Long packageId = tenantRepository.findByTenantId(currentTenantId)
                    .map(com.mok.ddd.domain.sys.model.Tenant::getPackageId)
                    .orElse(null);

            if (packageId != null) {
                Set<Long> allowedMenuIds = tenantPackageService.getMenuIdsByPackage(packageId);
                Set<Long> allowedPermissionIds = tenantPackageService.getPermissionIdsByPackage(packageId);

                entities = entities.stream()
                        .filter(m -> allowedMenuIds.contains(m.getId()))
                        .toList();

                List<MenuOptionDTO> flatList = entities.stream()
                        .map(entity -> {
                            MenuOptionDTO dto = new MenuOptionDTO();
                            dto.setId(entity.getId());
                            dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
                            dto.setName(entity.getName());
                            dto.setPath(entity.getPath());
                            dto.setIsPermission(false);

                            if (entity.getPermissions() != null) {
                                List<PermissionOptionDTO> pDtos = entity.getPermissions().stream()
                                        .filter(p -> allowedPermissionIds.contains(p.getId()))
                                        .map(p -> {
                                            PermissionOptionDTO pDto = new PermissionOptionDTO();
                                            pDto.setId(p.getId());
                                            pDto.setName(p.getName());
                                            pDto.setIsPermission(true);
                                            return pDto;
                                        }).toList();
                                dto.setPermissions(pDtos);
                            }
                            return dto;
                        }).toList();

                return buildTreeFromFlatList(flatList);
            }
            return Collections.emptyList();
        }

        // 超级租户逻辑保持不变
        List<MenuOptionDTO> flatList = entities.stream()
                .map(entity -> {
                    MenuOptionDTO dto = new MenuOptionDTO();
                    dto.setId(entity.getId());
                    dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
                    dto.setName(entity.getName());
                    dto.setPath(entity.getPath());
                    dto.setIsPermission(false);

                    if (entity.getPermissions() != null) {
                        List<PermissionOptionDTO> pDtos = entity.getPermissions().stream()
                                .map(p -> {
                                    PermissionOptionDTO pDto = new PermissionOptionDTO();
                                    pDto.setId(p.getId());
                                    pDto.setName(p.getName());
                                    pDto.setIsPermission(true);
                                    return pDto;
                                }).toList();
                        dto.setPermissions(pDtos);
                    }
                    return dto;
                }).toList();

        return buildTreeFromFlatList(flatList);
    }

    private List<MenuOptionDTO> buildTreeFromFlatList(List<MenuOptionDTO> flatList) {
        Map<Long, MenuOptionDTO> dtoMap = flatList.stream()
                .collect(Collectors.toMap(MenuOptionDTO::getId, dto -> dto));

        List<MenuOptionDTO> rootMenus = new ArrayList<>();

        for (MenuOptionDTO dto : flatList) {
            if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
                if (dto.getChildren() == null) {
                    dto.setChildren(new ArrayList<>());
                }
                for (PermissionOptionDTO perm : dto.getPermissions()) {
                    MenuOptionDTO permNode = new MenuOptionDTO();
                    permNode.setId(perm.getId());
                    permNode.setName("[按钮] " + perm.getName());
                    permNode.setIsPermission(true);
                    dto.getChildren().add(permNode);
                }
            }

            if (dto.getParentId() == null || dto.getParentId() == 0) {
                rootMenus.add(dto);
            } else {
                MenuOptionDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }
        return filterEmptyParentMenuOptions(rootMenus);
    }

    private List<MenuOptionDTO> filterEmptyParentMenuOptions(@NonNull List<MenuOptionDTO> menus) {
        List<MenuOptionDTO> filtered = new ArrayList<>();
        for (MenuOptionDTO menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                menu.setChildren(filterEmptyParentMenuOptions(menu.getChildren()));
            }

            boolean hasChildren = menu.getChildren() != null && !menu.getChildren().isEmpty();
            boolean hasPath = menu.getPath() != null && !menu.getPath().isEmpty();
            boolean isButton = Boolean.TRUE.equals(menu.getIsPermission());

            if (isButton || hasPath || hasChildren) {
                filtered.add(menu);
            }
        }
        return filtered;
    }

    @Override
    protected CustomRepository<Menu, Long> getRepository() {
        return menuRepository;
    }

    @Override
    protected Menu toEntity(@NonNull MenuDTO menuDTO) {
        return menuMapper.toEntity(menuDTO);
    }

    @Override
    protected MenuDTO toDto(@NonNull Menu entity) {
        return menuMapper.toDto(entity);
    }
}