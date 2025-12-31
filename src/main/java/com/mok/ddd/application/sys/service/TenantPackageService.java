package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageGrantDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageOptionDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageSaveDTO;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.application.sys.mapper.PermissionMapper;
import com.mok.ddd.application.sys.mapper.TenantPackageMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.model.QTenantPackage;
import com.mok.ddd.domain.sys.model.TenantPackage;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.domain.sys.repository.TenantPackageRepository;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantPackageService extends BaseServiceImpl<TenantPackage, Long, TenantPackageDTO>{

    private final TenantPackageRepository packageRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final TenantPackageMapper packageMapper;
    private final TenantRepository tenantRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MenuMapper menuMapper;
    private final PermissionMapper permissionMapper;

    @Transactional
    public void createPackage(TenantPackageSaveDTO dto) {
        TenantPackage entity = packageMapper.toEntity(dto);
        packageRepository.save(entity);
    }

    @Transactional
    public void updatePackage(Long id, TenantPackageSaveDTO dto) {
        TenantPackage entity = packageRepository.findById(id)
                .orElseThrow(() -> new BizException("套餐不存在"));
        packageMapper.updateEntityFromDto(dto, entity);
        packageRepository.save(entity);
    }

    @Transactional
    public void grant(Long id, TenantPackageGrantDTO dto) {
        TenantPackage entity = packageRepository.findById(id)
                .orElseThrow(() -> new BizException("套餐不存在"));

        if (dto.getMenuIds() != null) {
            entity.setMenus(new HashSet<>(menuRepository.findAllById(dto.getMenuIds())));
        }
        if (dto.getPermissionIds() != null) {
            entity.setPermissions(new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds())));
        }
        packageRepository.save(entity);

        // 清理缓存
        Set<String> keys = Set.of(
                Const.CacheKey.TENANT_PACKAGE_PERMS + ":menus:" + id,
                Const.CacheKey.TENANT_PACKAGE_PERMS + ":permissions:" + id
        );
        redisTemplate.delete(keys);
    }

    @Transactional
    public TenantPackageDTO updateTenantState(@NonNull Long id, @NonNull Boolean state) {
        TenantPackage existingTenant = packageRepository.findById(id).orElseThrow(() -> new BizException("套餐不存在"));
        existingTenant.setEnabled(state);
        return packageMapper.toDto(packageRepository.save(existingTenant));
    }

    @Transactional
    public void deleteByVerify(@NonNull Long id) {
        TenantPackage existingTenant = packageRepository.findById(id).orElseThrow(() -> new BizException("套餐不存在"));
        long useCount = tenantRepository.countByPackageId(id);
        if (useCount > 0) {
            throw new BizException("套餐正在使用中，不允许删除");
        }
        packageRepository.delete(existingTenant);
    }

    @Transactional(readOnly = true)
    public List<TenantPackageOptionDTO> findOptions(String name){
        QTenantPackage tenantPackage = QTenantPackage.tenantPackage;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(name)) {
            builder.and(tenantPackage.name.containsIgnoreCase(name));
        }
        builder.and(tenantPackage.enabled.eq(true));
        List<TenantPackageDTO> list = findAll(builder);
        return packageMapper.dtoToOptionsDto(list);
    }

    @Transactional(readOnly = true)
    public Set<Long> getMenuIdsByPackage(Long id) {
        String cacheKey = Const.CacheKey.TENANT_PACKAGE_PERMS + ":menus:" + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Set) {
            return (Set<Long>) cached;
        }

        TenantPackage tenantPackage = packageRepository.findById(id).orElse(null);
        if (tenantPackage == null || tenantPackage.getMenus() == null) {
            return Collections.emptySet();
        }
        Set<Long> menuIds = tenantPackage.getMenus().stream().map(Menu::getId).collect(Collectors.toSet());
        redisTemplate.opsForValue().set(cacheKey, menuIds);
        return menuIds;
    }

    @Transactional(readOnly = true)
    public Set<Long> getPermissionIdsByPackage(Long id) {
        String cacheKey = Const.CacheKey.TENANT_PACKAGE_PERMS + ":permissions:" + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Set) {
            return (Set<Long>) cached;
        }

        TenantPackage tenantPackage = packageRepository.findById(id).orElse(null);
        if (tenantPackage == null || tenantPackage.getPermissions() == null) {
            return Collections.emptySet();
        }
        Set<Long> permissionIds = tenantPackage.getPermissions().stream().map(Permission::getId).collect(Collectors.toSet());
        redisTemplate.opsForValue().set(cacheKey, permissionIds);
        return permissionIds;
    }

    @Override
    @Transactional(readOnly = true)
    public TenantPackageDTO getById(Long id) {
        TenantPackage entity = packageRepository.findById(id)
                .orElseThrow(() -> new BizException("套餐不存在"));
        TenantPackageDTO dto = packageMapper.toDto(entity);

        if (entity.getMenus() != null) {
            dto.setMenus(entity.getMenus().stream()
                    .map(menuMapper::toDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setMenus(Collections.emptySet());
        }

        if (entity.getPermissions() != null) {
            dto.setPermissions(entity.getPermissions().stream()
                    .map(permissionMapper::toDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setPermissions(Collections.emptySet());
        }
        return dto;
    }

    @Override
    protected CustomRepository<TenantPackage, Long> getRepository() {
        return packageRepository;
    }

    @Override
    protected TenantPackage toEntity(@NonNull TenantPackageDTO tenantPackageDTO) {
        return packageMapper.toEntity(tenantPackageDTO);
    }

    @Override
    protected TenantPackageDTO toDto(@NonNull TenantPackage entity) {
        return packageMapper.toDto(entity);
    }
}
