package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantQuery;
import com.mok.ddd.application.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantCacheService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public TenantDTO findByTenantId(@NonNull String tenantId) {
        String key = Const.CacheKey.TENANT + tenantId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof TenantDTO tenantDTO) {
            return tenantDTO;
        }

        TenantQuery query = new TenantQuery();
        query.setTenantId(tenantId);
        TenantDTO dto = tenantRepository.findOne(query.toPredicate())
                .map(tenantMapper::toDto)
                .orElse(null);

        if (dto != null) {
            redisTemplate.opsForValue().set(key, dto);
        }
        return dto;
    }
}
