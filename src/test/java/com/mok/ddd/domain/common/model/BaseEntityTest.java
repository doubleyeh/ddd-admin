package com.mok.ddd.domain.common.model;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.infrastructure.util.SnowFlakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@DisplayName("BaseEntity 领域模型测试")
class BaseEntityTest {

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

    static class TestEntity extends BaseEntity {
        public void triggerPrePersist() {
            super.prePersist();
        }

        public void triggerPreUpdate() {
            super.preUpdate();
        }
        
        public void publicSetCreateBy(String createBy) {
            super.setCreateBy(createBy);
        }
        
        public void publicSetUpdateBy(String updateBy) {
            super.setUpdateBy(updateBy);
        }
    }

    @Test
    @DisplayName("prePersist - 自动填充字段")
    void prePersist_AutoFill() {
        long expectedId = 12345L;
        String username = "testUser";

        mockedIdGenerator.when(SnowFlakeIdGenerator::nextId).thenReturn(expectedId);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn(username);

        TestEntity entity = new TestEntity();
        entity.triggerPrePersist();

        assertEquals(expectedId, entity.getId());
        assertEquals(username, entity.getCreateBy());
        assertEquals(username, entity.getUpdateBy());
        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
    }

    @Test
    @DisplayName("prePersist - 已有值不覆盖")
    void prePersist_ExistingValues() {
        long expectedId = 12345L;
        String username = "testUser";
        String existingUser = "existingUser";

        mockedIdGenerator.when(SnowFlakeIdGenerator::nextId).thenReturn(expectedId);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn(username);

        TestEntity entity = new TestEntity();
        entity.publicSetCreateBy(existingUser);
        entity.publicSetUpdateBy(existingUser);
        
        entity.triggerPrePersist();

        assertEquals(expectedId, entity.getId());
        assertEquals(existingUser, entity.getCreateBy());
        assertEquals(existingUser, entity.getUpdateBy());
    }
    
    @Test
    @DisplayName("prePersist - 无用户名")
    void prePersist_NoUsername() {
        long expectedId = 12345L;
        mockedIdGenerator.when(SnowFlakeIdGenerator::nextId).thenReturn(expectedId);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn(null);

        TestEntity entity = new TestEntity();
        entity.triggerPrePersist();

        assertEquals(expectedId, entity.getId());
        assertNull(entity.getCreateBy());
        assertNull(entity.getUpdateBy());
    }

    @Test
    @DisplayName("preUpdate - 更新时间和修改人")
    void preUpdate_AutoFill() {
        String username = "updateUser";
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn(username);

        TestEntity entity = new TestEntity();
        entity.triggerPreUpdate();

        assertEquals(username, entity.getUpdateBy());
        assertNotNull(entity.getUpdateTime());
    }
    
    @Test
    @DisplayName("preUpdate - 已有修改人不覆盖")
    void preUpdate_ExistingUpdateBy() {
        String username = "updateUser";
        String existingUser = "existingUser";
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn(username);

        TestEntity entity = new TestEntity();
        entity.publicSetUpdateBy(existingUser);
        entity.triggerPreUpdate();

        assertEquals(existingUser, entity.getUpdateBy());
        assertNotNull(entity.getUpdateTime());
    }
}
