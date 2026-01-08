package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Tenant;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantCacheService 单元测试")
class TenantCacheServiceTest {

    @InjectMocks
    private TenantCacheService tenantCacheService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Test
    @DisplayName("findByTenantId - 缓存命中")
    void findByTenantId_CacheHit() {
        String tenantId = "test-tenant";
        String key = Const.CacheKey.TENANT + tenantId;
        TenantDTO cachedDto = new TenantDTO();
        cachedDto.setTenantId(tenantId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(cachedDto);

        TenantDTO result = tenantCacheService.findByTenantId(tenantId);

        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
        verify(tenantRepository, never()).findOne((Predicate) any());
    }

    @Test
    @DisplayName("findByTenantId - 缓存未命中，数据库找到")
    void findByTenantId_CacheMiss_DbHit() {
        String tenantId = "test-tenant";
        String key = Const.CacheKey.TENANT + tenantId;
        Tenant tenant = mock(Tenant.class);
        TenantDTO dtoFromDb = new TenantDTO();
        dtoFromDb.setTenantId(tenantId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);
        when(tenantRepository.findOne((Predicate) any())).thenReturn(Optional.of(tenant));
        when(tenantMapper.toDto(tenant)).thenReturn(dtoFromDb);

        TenantDTO result = tenantCacheService.findByTenantId(tenantId);

        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
        verify(valueOperations).set(key, dtoFromDb);
    }

    @Test
    @DisplayName("findByTenantId - 缓存和数据库均未命中")
    void findByTenantId_CacheMiss_DbMiss() {
        String tenantId = "test-tenant";
        String key = Const.CacheKey.TENANT + tenantId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);
        when(tenantRepository.findOne((Predicate) any())).thenReturn(Optional.empty());

        TenantDTO result = tenantCacheService.findByTenantId(tenantId);

        assertNull(result);
        verify(valueOperations, never()).set(any(), any());
    }
}
