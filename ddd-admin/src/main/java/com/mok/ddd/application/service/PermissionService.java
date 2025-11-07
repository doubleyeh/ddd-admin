package com.mok.ddd.application.service;

import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public Set<String> getAllPermissionCodes() {
        return permissionRepository.findAll().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }
}