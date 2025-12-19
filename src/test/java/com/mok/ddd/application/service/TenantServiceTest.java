package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;
import com.mok.ddd.domain.entity.Tenant;
import com.mok.ddd.domain.repository.TenantRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService 单元测试")
class TenantServiceTest {

    @InjectMocks
    private TenantService tenantService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private UserService userService;

    private MockedStatic<PasswordGenerator> mockedPasswordGenerator;

    private static final String MOCK_PASSWORD = "GeneratedMockPassword123";

    @BeforeEach
    void setUp() {
        mockedPasswordGenerator = mockStatic(PasswordGenerator.class);
        mockedPasswordGenerator.when(PasswordGenerator::generateRandomPassword).thenReturn(MOCK_PASSWORD);
    }

    @AfterEach
    void tearDown() {
        mockedPasswordGenerator.close();
    }

    @Nested
    @DisplayName("createTenant 租户创建测试")
    class CreateTenantTests {

        private TenantSaveDTO validDto;
        private Tenant mockTenant;

        @BeforeEach
        void setup() {
            validDto = new TenantSaveDTO();
            validDto.setName("新租户名称");
            validDto.setContactPerson("联系人");
            validDto.setContactPhone("13800000000");

            mockTenant = new Tenant();
            mockTenant.setId(1L);
            mockTenant.setTenantId("RANDOM");
            mockTenant.setName(validDto.getName());
            mockTenant.setContactPerson(validDto.getContactPerson());
            mockTenant.setContactPhone(validDto.getContactPhone());
            mockTenant.setEnabled(true);
        }

        @Test
        @DisplayName("成功创建租户和初始化管理员")
        void createTenant_Success() {
            when(tenantRepository.findByTenantId(anyString())).thenReturn(Optional.empty());
            when(tenantMapper.toEntity(validDto)).thenReturn(mockTenant);
            when(tenantRepository.save(any(Tenant.class))).thenReturn(mockTenant);

            TenantCreateResultDTO result = tenantService.createTenant(validDto);

            verify(tenantRepository, atLeastOnce()).findByTenantId(anyString());
            verify(tenantRepository, times(1)).save(any(Tenant.class));

            ArgumentCaptor<UserPostDTO> userCaptor = ArgumentCaptor.forClass(UserPostDTO.class);
            ArgumentCaptor<String> tenantIdCaptor = ArgumentCaptor.forClass(String.class);

            verify(userService, times(1)).createForTenant(userCaptor.capture(), tenantIdCaptor.capture());

            UserPostDTO capturedUserDTO = userCaptor.getValue();
            assertEquals(Const.DEFAULT_ADMIN_USERNAME, capturedUserDTO.getUsername());
            assertEquals(MOCK_PASSWORD, capturedUserDTO.getPassword());

            assertNotNull(result);
            assertEquals(mockTenant.getId(), result.getId());
            assertEquals(MOCK_PASSWORD, result.getInitialAdminPassword());
        }

        @Test
        @DisplayName("创建租户失败：生成唯一租户编码失败")
        void createTenant_GenerateIdFailed_ThrowsBizException() {
            when(tenantRepository.findByTenantId(anyString())).thenReturn(Optional.of(mockTenant));

            BizException exception = assertThrows(BizException.class, () -> {
                tenantService.createTenant(validDto);
            });

            assertEquals("生成唯一租户编码失败，请重试", exception.getMessage());
            verify(tenantRepository, times(5)).findByTenantId(anyString());
            verify(tenantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateTenant 租户更新测试")
    class UpdateTenantTests {

        @Test
        @DisplayName("更新失败：尝试修改租户ID")
        void updateTenant_TenantIdChanged_ThrowsBizException() {
            Long existingId = 1L;
            TenantSaveDTO invalidDto = new TenantSaveDTO();
            invalidDto.setTenantId("new-id");
            invalidDto.setName("name");

            Tenant existingTenant = new Tenant();
            existingTenant.setId(existingId);
            existingTenant.setTenantId("old-id");

            when(tenantRepository.findById(existingId)).thenReturn(Optional.of(existingTenant));

            BizException exception = assertThrows(BizException.class, () -> {
                tenantService.updateTenant(existingId, invalidDto);
            });

            assertEquals("租户编码不可修改", exception.getMessage());
            verify(tenantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("租户状态与删除测试")
    class StateAndDeleteTests {

        @Test
        @DisplayName("更新租户状态成功")
        void updateTenantState_Success() {
            Long id = 1L;
            Boolean newState = false;
            Tenant tenant = new Tenant();
            tenant.setId(id);
            tenant.setEnabled(true);

            when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
            when(tenantMapper.toDto(any(Tenant.class))).thenReturn(new TenantDTO());

            tenantService.updateTenantState(id, newState);

            verify(tenantRepository).save(tenant);
            assertFalse(tenant.getEnabled());
        }

        @Test
        @DisplayName("验证删除租户成功")
        void deleteByVerify_Success() {
            Long id = 1L;
            TenantDTO dto = new TenantDTO();
            dto.setTenantId("NORMAL_001");

            TenantService spyService = spy(tenantService);
            doReturn(dto).when(spyService).getById(id);
            doNothing().when(spyService).deleteById(id);

            boolean result = spyService.deleteByVerify(id);

            verify(spyService).deleteById(id);
            assertTrue(result);
        }

        @Test
        @DisplayName("删除租户失败：超管租户不可删除")
        void deleteByVerify_SuperTenant_ThrowsBizException() {
            Long id = 1L;
            TenantDTO superTenantDto = new TenantDTO();
            superTenantDto.setTenantId("000000");

            TenantService spyService = spy(tenantService);
            doReturn(superTenantDto).when(spyService).getById(id);

            BizException exception = assertThrows(BizException.class, () -> spyService.deleteByVerify(id));
            assertEquals("该租户不可删除", exception.getMessage());
            verify(spyService, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("查询测试")
    class QueryTests {

        @Test
        @DisplayName("获取租户选项列表成功")
        void findOptions_Success() {
            String name = "测试";
            TenantDTO dto = new TenantDTO();
            dto.setName("测试租户");

            List<TenantDTO> dtoList = List.of(dto);
            TenantOptionDTO option = new TenantOptionDTO();
            option.setName("测试租户");

            when(tenantRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(new Tenant()));

            when(tenantMapper.dtoToOptionsDto(any())).thenReturn(List.of(option));

            List<TenantOptionDTO> result = tenantService.findOptions(name);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试租户", result.getFirst().getName());
        }
    }
}