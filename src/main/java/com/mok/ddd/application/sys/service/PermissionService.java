package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import com.mok.ddd.application.sys.mapper.PermissionMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermissionService extends BaseServiceImpl<Permission, Long, PermissionDTO> {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public Set<String> getAllPermissionCodes() {
        return permissionRepository.findAll().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getPermissionsByRoleIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }

        return roleIds.stream().flatMap(roleId -> {
            String cacheKey = Const.CacheKey.ROLE_PERMS + ":" + roleId;
            List<String> cachedPerms = redisTemplate.opsForList().range(cacheKey, 0, -1);

            if (cachedPerms == null || cachedPerms.isEmpty()) {
                List<String> dbPerms = permissionRepository.findCodesByRoleId(roleId);
                if (!dbPerms.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(cacheKey, dbPerms);
                    return dbPerms.stream();
                }
                return Stream.empty();
            }
            return cachedPerms.stream();
        }).collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        List<Long> roleIds = permissionRepository.findRoleIdsByPermissionId(id);

        permissionRepository.deleteRolePermissionsByPermissionId(id);
        permissionRepository.deleteById(id);

        if (!roleIds.isEmpty()) {
            List<String> keys = roleIds.stream()
                    .map(roleId -> Const.CacheKey.ROLE_PERMS + ":" + roleId)
                    .toList();
            redisTemplate.delete(keys);
        }
    }

    @Override
    protected CustomRepository<Permission, Long> getRepository() {
        return permissionRepository;
    }

    @Override
    protected Permission toEntity(@NonNull PermissionDTO permissionDTO) {
        return permissionMapper.toEntity(permissionDTO);
    }

    @Override
    protected PermissionDTO toDto(@NonNull Permission entity) {
        return permissionMapper.toDto(entity);
    }
}