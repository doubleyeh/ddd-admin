package com.mok.ddd.domain.sys.model;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenant.TenantSaveDTO;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TenantTest {

    private Tenant createTestTenant(String tenantId, String name, Integer state, Long packageId) {
        try {
            var constructor = Tenant.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Tenant tenant = constructor.newInstance();
            setField(tenant, "tenantId", tenantId);
            setField(tenant, "name", name);
            setField(tenant, "state", state);
            setField(tenant, "packageId", packageId);
            return tenant;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = Tenant.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("create 方法测试")
    class CreateTests {

        @Test
        void create_Success() {
            TenantRepository mockRepo = mock(TenantRepository.class);
            when(mockRepo.findByTenantId(anyString())).thenReturn(Optional.empty());

            TenantSaveDTO dto = new TenantSaveDTO();
            dto.setName("Test Tenant");
            dto.setPackageId(1L);
            dto.setContactPerson("CP");
            dto.setContactPhone("123");

            Tenant tenant = Tenant.create(dto, mockRepo);

            assertNotNull(tenant);
            assertEquals("Test Tenant", tenant.getName());
            assertEquals(1L, tenant.getPackageId());
            assertEquals(Const.TenantState.NORMAL, tenant.getState());
            assertNotNull(tenant.getTenantId());
            assertEquals(6, tenant.getTenantId().length());
        }

        @Test
        void create_FailsWhenPackageIdIsNull() {
            TenantRepository mockRepo = mock(TenantRepository.class);
            TenantSaveDTO dto = new TenantSaveDTO();
            dto.setName("Test Tenant");
            dto.setPackageId(null);

            BizException exception = assertThrows(BizException.class, () -> Tenant.create(dto, mockRepo));
            assertEquals("套餐不能为空", exception.getMessage());
        }

        @Test
        void create_FailsWhenIdGenerationFails() {
            TenantRepository mockRepo = mock(TenantRepository.class);
            when(mockRepo.findByTenantId(anyString())).thenReturn(Optional.of(mock(Tenant.class)));

            TenantSaveDTO dto = new TenantSaveDTO();
            dto.setName("Test Tenant");
            dto.setPackageId(1L);

            BizException exception = assertThrows(BizException.class, () -> Tenant.create(dto, mockRepo));
            assertEquals("生成唯一租户编码失败，请重试", exception.getMessage());
            verify(mockRepo, times(5)).findByTenantId(anyString());
        }
    }

    @Nested
    @DisplayName("disable 方法测试")
    class DisableTests {
        @Test
        void disable_NormalTenant_ShouldSetStateToDisabled() {
            Tenant tenant = createTestTenant("NORMAL_ID", "Normal Tenant", Const.TenantState.NORMAL, 1L);
            tenant.disable();
            assertEquals(Const.TenantState.DISABLED, tenant.getState());
        }

        @Test
        void disable_SuperTenant_ShouldThrowBizException() {
            Tenant superTenant = createTestTenant("000000", "Super Tenant", Const.TenantState.NORMAL, null);
            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperTenant("000000")).thenReturn(true);
                BizException exception = assertThrows(BizException.class, superTenant::disable);
                assertEquals("无法对该租户进行操作", exception.getMessage());
            }
        }

        @Test
        void disable_AlreadyDisabledTenant_ShouldRemainDisabled() {
            Tenant tenant = createTestTenant("NORMAL_ID", "Normal Tenant", Const.TenantState.DISABLED, 1L);
            tenant.disable();
            assertEquals(Const.TenantState.DISABLED, tenant.getState());
        }
    }

    @Nested
    @DisplayName("enable 方法测试")
    class EnableTests {
        @Test
        void enable_DisabledTenant_ShouldSetStateToNormal() {
            Tenant tenant = createTestTenant("NORMAL_ID", "Normal Tenant", Const.TenantState.DISABLED, 1L);
            tenant.enable();
            assertEquals(Const.TenantState.NORMAL, tenant.getState());
        }

        @Test
        void enable_AlreadyEnabledTenant_ShouldRemainNormal() {
            Tenant tenant = createTestTenant("NORMAL_ID", "Normal Tenant", Const.TenantState.NORMAL, 1L);
            tenant.enable();
            assertEquals(Const.TenantState.NORMAL, tenant.getState());
        }
    }

    @Nested
    @DisplayName("changePackage 方法测试")
    class ChangePackageTests {
        @Test
        void changePackage_NormalTenant_ShouldUpdatePackageId() {
            Tenant tenant = createTestTenant("NORMAL_ID", "Normal Tenant", Const.TenantState.NORMAL, 1L);
            tenant.changePackage(2L);
            assertEquals(2L, tenant.getPackageId());
        }

        @Test
        void changePackage_SuperTenant_ShouldDoNothing() {
            Tenant superTenant = createTestTenant("000000", "Super Tenant", Const.TenantState.NORMAL, null);
            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperTenant("000000")).thenReturn(true);
                superTenant.changePackage(1L);
                assertNull(superTenant.getPackageId());
            }
        }

        @Test
        void changePackage_WithNullPackageId_ShouldThrowBizException() {
            Tenant tenant = createTestTenant("NORMAL_ID", "Normal Tenant", Const.TenantState.NORMAL, 1L);
            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperTenant("NORMAL_ID")).thenReturn(false);
                BizException exception = assertThrows(BizException.class, () -> tenant.changePackage(null));
                assertEquals("套餐不能为空", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("updateInfo 方法测试")
    class UpdateInfoTests {
        @Test
        void updateInfo_ShouldUpdateFields() {
            Tenant tenant = createTestTenant("ID", "Old Name", Const.TenantState.NORMAL, 1L);
            try {
                setField(tenant, "contactPerson", "Old Person");
                setField(tenant, "contactPhone", "111");
            } catch (Exception e) {
                fail(e);
            }

            tenant.updateInfo("New Name", "New Person", "222");

            assertEquals("New Name", tenant.getName());
            assertEquals("New Person", tenant.getContactPerson());
            assertEquals("222", tenant.getContactPhone());
        }
    }
}
