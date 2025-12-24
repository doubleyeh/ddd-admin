package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.dto.tenantPackage.TenantPackageOptionDTO;
import com.mok.ddd.application.dto.tenantPackage.TenantPackageSaveDTO;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.mapper.TenantPackageMapper;
import com.mok.ddd.domain.entity.TenantPackage;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.domain.repository.PermissionRepository;
import com.mok.ddd.domain.repository.TenantPackageRepository;
import com.mok.ddd.domain.repository.TenantRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;

import static com.mok.ddd.domain.entity.QTenant.tenant;

@Service
@RequiredArgsConstructor
public class TenantPackageService extends BaseServiceImpl<TenantPackage, Long, TenantPackageDTO>{

    private final TenantPackageRepository packageRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final TenantPackageMapper packageMapper;
    private final TenantRepository tenantRepository;

    @Transactional
    public void createPackage(TenantPackageSaveDTO dto) {
        TenantPackage entity = packageMapper.toEntity(dto);
        this.syncRelations(entity, dto);
        packageRepository.save(entity);
    }

    @Transactional
    public void updatePackage(Long id, TenantPackageSaveDTO dto) {
        TenantPackage entity = packageRepository.findById(id)
                .orElseThrow(() -> new BizException("套餐不存在"));
        packageMapper.updateEntityFromDto(dto, entity);
        this.syncRelations(entity, dto);
        packageRepository.save(entity);
    }

    private void syncRelations(TenantPackage entity, TenantPackageSaveDTO dto) {
        if (dto.getMenuIds() != null) {
            entity.setMenus(new HashSet<>(menuRepository.findAllById(dto.getMenuIds())));
        }
        if (dto.getPermissionIds() != null) {
            entity.setPermissions(new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds())));
        }
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
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(name)) {
            builder.and(tenant.name.containsIgnoreCase(name));
        }
        builder.and(tenant.enabled.eq(true));
        List<TenantPackageDTO> list = findAll(builder);
        return packageMapper.dtoToOptionsDto(list);
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
