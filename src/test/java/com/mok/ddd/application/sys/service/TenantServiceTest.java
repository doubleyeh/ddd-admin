package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.sys.event.TenantCreatedEvent;
import com.mok.ddd.application.sys.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;
import com.mok.ddd.domain.sys.model.Tenant;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private ApplicationEventPublisher eventPublisher;

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
        @DisplayName("成功创建租户并发布事件")
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

            ArgumentCaptor<TenantCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TenantCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            TenantCreatedEvent capturedEvent = eventCaptor.getValue();

            assertEquals(mockTenant, capturedEvent.getTenant());
            assertEquals(MOCK_PASSWORD, capturedEvent.getRawPassword());

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
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("updateTenant")
    class UpdateTenantTests {

        @Test
        @DisplayName("更新失败：租户不存在")
        void updateTenant_NotFound_ThrowsBizException() {
            Long id = 1L;
            TenantSaveDTO dto = new TenantSaveDTO();
            when(tenantRepository.findById(id)).thenReturn(Optional.empty());

            BizException exception = assertThrows(BizException.class, () -> tenantService.updateTenant(id, dto));
            assertEquals("租户不存在", exception.getMessage());
        }

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
        @DisplayName("更新租户状态失败：租户不存在")
        void updateTenantState_NotFound_ThrowsBizException() {
            Long id = 1L;
            Integer state = Const.TenantState.NORMAL;
            when(tenantRepository.findById(id)).thenReturn(Optional.empty());

            BizException exception = assertThrows(BizException.class, () -> tenantService.updateTenantState(id, state));
            assertEquals("租户不存在", exception.getMessage());
        }

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
        @DisplayName("更新租户状态失败：无效的状态值")
        void updateTenantState_InvalidState_ThrowsBizException() {
            Long id = 1L;
            Integer invalidState = 999;
            Tenant tenant = mock(Tenant.class);

            when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));

            BizException exception = assertThrows(BizException.class, () -> tenantService.updateTenantState(id, invalidState));
            assertEquals("无效的状态值: " + invalidState, exception.getMessage());
            verify(tenantRepository, never()).save(any());
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
        @DisplayName("删除租户失败：租户不存在")
        void deleteByVerify_NotFound_ThrowsBizException() {
            Long id = 1L;
            TenantService spyService = spy(tenantService);
            doReturn(null).when(spyService).getById(id);

            BizException exception = assertThrows(BizException.class, () -> spyService.deleteByVerify(id));
            assertEquals("租户不存在", exception.getMessage());
            verify(spyService, never()).deleteById(any());
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

        @Test
        @DisplayName("获取租户选项列表成功：名称为空")
        void findOptions_NullName_Success() {
            TenantOptionDTO option = new TenantOptionDTO();
            option.setName("测试租户");

            when(tenantRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(mock(Tenant.class)));

            when(tenantMapper.dtoToOptionsDto(any())).thenReturn(List.of(option));

            List<TenantOptionDTO> result = tenantService.findOptions(null);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findPage 分页查询测试")
        void findPage_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Predicate predicate = mock(Predicate.class);
            TenantDTO tenantDTO = new TenantDTO();
            tenantDTO.setId(1L);

            JPAQueryFactory listFactory = mock(JPAQueryFactory.class);
            JPAQueryFactory countFactory = mock(JPAQueryFactory.class);
            JPAQuery<TenantDTO> listQuery = mock(JPAQuery.class);
            JPAQuery<Long> countQuery = mock(JPAQuery.class);
            JPQLQuery<TenantDTO> paginatedQuery = mock(JPQLQuery.class);

            when(tenantRepository.getJPAQueryFactory()).thenReturn(listFactory).thenReturn(countFactory);

            when(listFactory.select(any(Expression.class))).thenReturn(listQuery);
            when(listQuery.from(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.leftJoin(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.on(any(Predicate.class))).thenReturn(listQuery);
            when(listQuery.where(any(Predicate.class))).thenReturn(listQuery);

            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);
            when(tenantRepository.getQuerydsl()).thenReturn(querydsl);
            when(querydsl.applyPagination(any(), eq(listQuery))).thenReturn(paginatedQuery);
            when(paginatedQuery.fetch()).thenReturn(Collections.singletonList(tenantDTO));

            when(countFactory.select(any(Expression.class))).thenReturn(countQuery);
            when(countQuery.from(any(EntityPath.class))).thenReturn(countQuery);
            when(countQuery.where(any(Predicate.class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(100L);

            Page<TenantDTO> result = tenantService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(100L, result.getTotalElements());
        }
    }
    
    @Test
    @DisplayName("辅助方法测试")
    void helperMethods_Test() {
        assertEquals(tenantRepository, tenantService.getRepository());
        
        assertEquals("tenant", tenantService.getEntityAlias());
        
        Tenant tenant = mock(Tenant.class);
        TenantDTO dto = new TenantDTO();
        when(tenantMapper.toDto(tenant)).thenReturn(dto);
        assertEquals(dto, tenantService.toDto(tenant));
    }
}
