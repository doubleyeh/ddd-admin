package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.Tenant;
import com.mok.ddd.domain.repository.TenantRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TenantService extends BaseServiceImpl<Tenant, Long, TenantDTO> {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final UserService userService;

    @Override
    @NonNull
    protected CustomRepository<Tenant, Long> getRepository() {
        return tenantRepository;
    }

    @Override
    protected Tenant toEntity(@NonNull TenantDTO dto) {
        return tenantMapper.toEntity(dto);
    }

    @Override
    protected TenantDTO toDto(@NonNull Tenant entity) {
        return tenantMapper.toDto(entity);
    }

    @Transactional
    public TenantCreateResultDTO createTenant(@NonNull TenantSaveDTO dto) {
        Tenant tenant = tenantMapper.toEntity(dto);

        int maxRetry = 5;
        int attempt = 0;
        String tenantId;
        final String ALPHANUMERIC_UPPER = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();

        do {
            if (attempt++ >= maxRetry) {
                throw new BizException("生成唯一租户编码失败，请重试");
            }

            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHANUMERIC_UPPER.charAt(random.nextInt(ALPHANUMERIC_UPPER.length())));
            }
            tenantId = sb.toString();

        } while (tenantRepository.findByTenantId(tenantId).isPresent());

        tenant.setTenantId(tenantId);
        tenant = tenantRepository.save(tenant);

        String rawPassword = PasswordGenerator.generateRandomPassword();

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(Const.DEFAULT_ADMIN_USERNAME);
        userPostDTO.setNickname(tenant.getName() + "管理员");
        userPostDTO.setPassword(rawPassword);
        userPostDTO.setState(1);

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
    public TenantDTO updateTenant(@NonNull Long id,@NonNull TenantSaveDTO dto) {
        Tenant existingTenant = tenantRepository.findById(id).orElseThrow(() -> new BizException("租户不存在"));

        if (!existingTenant.getTenantId().equals(dto.getTenantId())) {
            throw new BizException("租户编码不可修改");
        }

        tenantMapper.updateEntityFromDto(dto, existingTenant);
        return tenantMapper.toDto(tenantRepository.save(existingTenant));
    }

    @Transactional
    public boolean deleteByVerify(@NonNull Long id){
        TenantDTO old = getById(id);
        if(Objects.isNull(old)){
            throw new BizException("租户不存在");
        }
        if (SysUtil.isSuperTenant(old.getTenantId())) {
            throw new BizException("该租户不可删除");
        }

        // TODO其他业务数据判断
        deleteById(id);
        return true;
    }
}