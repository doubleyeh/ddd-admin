package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.mapper.PermissionMapper;
import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.repository.PermissionRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService extends BaseServiceImpl<Permission, Long, PermissionDTO> {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Transactional(readOnly = true)
    public Set<String> getAllPermissionCodes() {
        return permissionRepository.findAll().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
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