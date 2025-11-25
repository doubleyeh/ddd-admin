package com.mok.ddd.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;
import com.mok.ddd.domain.entity.Tenant;
import com.mok.ddd.domain.repository.TenantRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService extends BaseServiceImpl<Tenant, Long, TenantDTO> {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final UserService userService;

    @Override
    protected CustomRepository<Tenant, Long> getRepository() {
        return tenantRepository;
    }

    @Override
    protected Tenant toEntity(TenantDTO dto) {
        return tenantMapper.toEntity(dto);
    }

    @Override
    protected TenantDTO toDto(Tenant entity) {
        return tenantMapper.toDto(entity);
    }

    @Transactional
    public TenantCreateResultDTO createTenant(TenantSaveDTO dto) {
        if (tenantRepository.findByTenantId(dto.getTenantId()).isPresent()) {
            throw new BizException("租户ID已存在");
        }

        Tenant tenant = tenantMapper.toEntity(dto);
        tenant = tenantRepository.save(tenant);

        String rawPassword = PasswordGenerator.generateRandomPassword();

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(Const.DEFAULT_ADMIN_USERNAME);
        userPostDTO.setNickname(tenant.getName() + "管理员");
        userPostDTO.setPassword(rawPassword);

        userService.createForTenant(userPostDTO, tenant.getTenantId());

        TenantCreateResultDTO result = new TenantCreateResultDTO();
        result.setId(tenant.getId());
        result.setTenantId(tenant.getTenantId());
        result.setName(tenant.getName());
        result.setContactPerson(tenant.getContactPerson());
        result.setContactPhone(tenant.getContactPhone());
        result.setEnabled(tenant.getEnabled());
        result.setInitialAdminPassword(rawPassword);

        return result;
    }

    @Transactional
    public TenantDTO updateTenant(Long id, TenantSaveDTO dto) {
        Tenant existingTenant = tenantRepository.findById(id).orElseThrow(() -> new BizException("租户不存在"));

        if (!existingTenant.getTenantId().equals(dto.getTenantId())) {
            throw new BizException("租户ID不可修改");
        }

        tenantMapper.updateEntityFromDto(dto, existingTenant);
        return tenantMapper.toDto(tenantRepository.save(existingTenant));
    }
}