package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.sys.dto.user.UserPostDTO;
import com.mok.ddd.application.sys.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;
import com.mok.ddd.domain.sys.model.Tenant;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

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

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private MockedStatic<PasswordGenerator> mockedPasswordGenerator;
    private MockedStatic<Tenant> mockedTenant;


    private static final String MOCK_PASSWORD = "GeneratedMockPassword123";

    @BeforeEach
    void setUp() {
        mockedPasswordGenerator = mockStatic(PasswordGenerator.class);
        mockedPasswordGenerator.when(PasswordGenerator::generateRandomPassword).thenReturn(MOCK_PASSWORD);
        mockedTenant = mockStatic(Tenant.class);
    }

    @AfterEach
    void tearDown() {
        mockedPasswordGenerator.close();
        mockedTenant.close();
    }

    @Nested
    @DisplayName("createTenant 租户创建测试")
    class CreateTenantTests {

        private TenantSaveDTO validDto;

        @BeforeEach
        void setup() {
            validDto = new TenantSaveDTO();
            validDto.setName("新租户名称");
            validDto.setContactPerson("联系人");
            validDto.setContactPhone("13800000000");
            validDto.setPackageId(1L);
        }

        @Test
        @DisplayName("成功创建租户和初始化管理员")
        void createTenant_Success() {
            Tenant mockTenant = mock(Tenant.class);
            String expectedTenantId = "MOCKID";
            when(mockTenant.getId()).thenReturn(1L);
            when(mockTenant.getTenantId()).thenReturn(expectedTenantId);
            when(mockTenant.getName()).thenReturn(validDto.getName());
            when(mockTenant.getState()).thenReturn(Const.TenantState.NORMAL);

            mockedTenant.when(() -> Tenant.create(
                    validDto.getName(),
                    validDto.getContactPerson(),
                    validDto.getContactPhone(),
                    validDto.getPackageId(),
                    tenantRepository
            )).thenReturn(mockTenant);
            when(tenantRepository.save(any(Tenant.class))).thenReturn(mockTenant);

            TenantCreateResultDTO result = tenantService.createTenant(validDto);

            mockedTenant.verify(() -> Tenant.create(
                    validDto.getName(),
                    validDto.getContactPerson(),
                    validDto.getContactPhone(),
                    validDto.getPackageId(),
                    tenantRepository
            ));
            verify(tenantRepository, times(1)).save(mockTenant);

            ArgumentCaptor<UserPostDTO> userCaptor = ArgumentCaptor.forClass(UserPostDTO.class);
            verify(userService, times(1)).createForTenant(userCaptor.capture(), eq(expectedTenantId));

            UserPostDTO capturedUserDTO = userCaptor.getValue();
            assertEquals(Const.DEFAULT_ADMIN_USERNAME, capturedUserDTO.getUsername());
            assertEquals(MOCK_PASSWORD, capturedUserDTO.getPassword());

            assertNotNull(result);
            assertEquals(mockTenant.getId(), result.getId());
            assertEquals(MOCK_PASSWORD, result.getInitialAdminPassword());
        }

        @Test
        @DisplayName("创建租户失败：当租户创建逻辑抛出异常")
        void createTenant_CreationLogicFails_ThrowsBizException() {
            BizException thrownException = new BizException("生成唯一租户编码失败，请重试");
            mockedTenant.when(() -> Tenant.create(any(), any(), any(), any(), any())).thenThrow(thrownException);

            BizException exception = assertThrows(BizException.class, () -> tenantService.createTenant(validDto));

            assertEquals("生成唯一租户编码失败，请重试", exception.getMessage());
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

            Tenant existingTenant = mock(Tenant.class);
            when(existingTenant.getTenantId()).thenReturn("old-id");

            when(tenantRepository.findById(existingId)).thenReturn(Optional.of(existingTenant));

            BizException exception = assertThrows(BizException.class, () -> tenantService.updateTenant(existingId, invalidDto));

            assertEquals("租户编码不可修改", exception.getMessage());
            verify(tenantRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新成功：调用领域对象的更新方法")
        void updateTenant_Success_CallsDomainMethods() {
            Long existingId = 1L;
            TenantSaveDTO dto = new TenantSaveDTO();
            dto.setTenantId("same-id");
            dto.setName("new name");
            dto.setContactPerson("new person");
            dto.setContactPhone("13900000000");
            dto.setPackageId(2L);

            Tenant existingTenant = mock(Tenant.class);
            when(existingTenant.getTenantId()).thenReturn("same-id");

            when(tenantRepository.findById(existingId)).thenReturn(Optional.of(existingTenant));
            when(tenantRepository.save(existingTenant)).thenReturn(existingTenant);
            when(tenantMapper.toDto(existingTenant)).thenReturn(new TenantDTO());

            tenantService.updateTenant(existingId, dto);

            verify(existingTenant).updateInfo(dto.getName(), dto.getContactPerson(), dto.getContactPhone());
            verify(existingTenant).changePackage(dto.getPackageId());
            verify(tenantRepository).save(existingTenant);
            verify(redisTemplate).delete(anyString());
        }
    }

    @Nested
    @DisplayName("租户状态与删除测试")
    class StateAndDeleteTests {

        @Test
        @DisplayName("更新租户状态为禁用成功")
        void updateTenantState_ToDisabled_Success() {
            Long id = 1L;
            Integer newState = Const.TenantState.DISABLED;
            Tenant tenant = mock(Tenant.class);

            when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
            when(tenantMapper.toDto(any(Tenant.class))).thenReturn(new TenantDTO());

            tenantService.updateTenantState(id, newState);

            verify(tenant).disable();
            verify(tenant, never()).enable();
            verify(tenantRepository).save(tenant);
        }

        @Test
        @DisplayName("更新租户状态为启用成功")
        void updateTenantState_ToEnabled_Success() {
            Long id = 1L;
            Integer newState = Const.TenantState.NORMAL;
            Tenant tenant = mock(Tenant.class);

            when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
            when(tenantMapper.toDto(any(Tenant.class))).thenReturn(new TenantDTO());

            tenantService.updateTenantState(id, newState);

            verify(tenant).enable();
            verify(tenant, never()).disable();
            verify(tenantRepository).save(tenant);
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
            TenantOptionDTO option = new TenantOptionDTO();
            option.setName("测试租户");

            when(tenantRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(mock(Tenant.class)));

            when(tenantMapper.dtoToOptionsDto(any())).thenReturn(List.of(option));

            List<TenantOptionDTO> result = tenantService.findOptions(name);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试租户", result.getFirst().getName());
        }
    }
}
