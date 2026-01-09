package com.mok.ddd.domain.common.model;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.infrastructure.util.SnowFlakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

@DisplayName("TenantBaseEntity 领域模型测试")
class TenantBaseEntityTest {

    private MockedStatic<TenantContextHolder> mockedTenantContext;
    private MockedStatic<SnowFlakeIdGenerator> mockedIdGenerator;

    @BeforeEach
    void setUp() {
        mockedTenantContext = mockStatic(TenantContextHolder.class);
        mockedIdGenerator = mockStatic(SnowFlakeIdGenerator.class);
    }

    @AfterEach
    void tearDown() {
        mockedTenantContext.close();
        mockedIdGenerator.close();
    }

    static class TestTenantEntity extends TenantBaseEntity {
        public void triggerPrePersist() {
            super.prePersist();
        }
        
        public void publicSetTenantId(String tenantId) {
            super.setTenantId(tenantId);
        }
    }

    @Test
    @DisplayName("prePersist - 自动填充租户ID")
    void prePersist_AutoFillTenantId() {
        String tenantId = "tenant-123";
        mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn(tenantId);
        mockedIdGenerator.when(SnowFlakeIdGenerator::nextId).thenReturn(1L);

        TestTenantEntity entity = new TestTenantEntity();
        entity.triggerPrePersist();

        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    @DisplayName("prePersist - 已有租户ID不覆盖")
    void prePersist_ExistingTenantId() {
        String tenantId = "tenant-123";
        String existingTenantId = "existing-tenant";
        mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn(tenantId);
        mockedIdGenerator.when(SnowFlakeIdGenerator::nextId).thenReturn(1L);

        TestTenantEntity entity = new TestTenantEntity();
        entity.publicSetTenantId(existingTenantId);
        entity.triggerPrePersist();

        assertEquals(existingTenantId, entity.getTenantId());
    }
}
