package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.tenantPackage.*;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.PermissionMapper;
import com.mok.ddd.application.mapper.TenantPackageMapper;
import com.mok.ddd.domain.entity.TenantPackage;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.domain.repository.PermissionRepository;
import com.mok.ddd.domain.repository.TenantPackageRepository;
import com.mok.ddd.domain.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantPackageService 单元测试")
class TenantPackageServiceTest {

    @InjectMocks
    private TenantPackageService tenantPackageService;

    @Mock
    private TenantPackageRepository packageRepository;
    @Mock
    private TenantPackageMapper packageMapper;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private MenuMapper menuMapper;
    @Mock
    private PermissionMapper permissionMapper;

    @Nested
    @DisplayName("查询业务测试")
    class QueryTests {
        @Test
        void findPage_ReturnSuccess() {
            TenantPackageQuery query = new TenantPackageQuery();
            PageRequest pageable = PageRequest.of(0, 10);
            Page<TenantPackage> entityPage = new PageImpl<>(Collections.emptyList());

            when(packageRepository.findAll(any(com.querydsl.core.types.Predicate.class), eq(pageable))).thenReturn(entityPage);

            Page<TenantPackageDTO> result = tenantPackageService.findPage(query.toPredicate(), pageable);

            assertNotNull(result);
        }

        @Test
        void getById_ReturnDto() {
            Long id = 1L;
            TenantPackage entity = new TenantPackage();
            entity.setMenus(Collections.emptySet());
            entity.setPermissions(Collections.emptySet());
            when(packageRepository.findById(id)).thenReturn(Optional.of(entity));
            when(packageMapper.toDto(entity)).thenReturn(new TenantPackageDTO());

            TenantPackageDTO result = tenantPackageService.getById(id);

            assertNotNull(result);
        }

        @Test
        void findOptions_ReturnList() {
            when(packageRepository.findAll(any(com.querydsl.core.types.Predicate.class))).thenReturn(Collections.emptyList());
            when(packageMapper.dtoToOptionsDto(any())).thenReturn(List.of(new TenantPackageOptionDTO()));

            List<TenantPackageOptionDTO> result = tenantPackageService.findOptions("test");

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("写操作业务测试")
    class WriteTests {
        @Test
        void createPackage_Success() {
            TenantPackageSaveDTO saveDto = new TenantPackageSaveDTO();
            TenantPackage entity = new TenantPackage();

            when(packageMapper.toEntity(saveDto)).thenReturn(entity);
            when(packageRepository.save(entity)).thenReturn(entity);
            //when(packageMapper.toDto(entity)).thenReturn(new TenantPackageDTO());

            tenantPackageService.createPackage(saveDto);

            verify(packageRepository).save(entity);
        }

        @Test
        void updatePackage_Success() {
            Long id = 1L;
            TenantPackageSaveDTO saveDto = new TenantPackageSaveDTO();
            TenantPackage entity = new TenantPackage();

            when(packageRepository.findById(id)).thenReturn(Optional.of(entity));
            when(packageRepository.save(entity)).thenReturn(entity);

            tenantPackageService.updatePackage(id, saveDto);

            verify(packageMapper).updateEntityFromDto(eq(saveDto), eq(entity));
            verify(packageRepository).save(entity);
        }

        @Test
        void grant_Success() {
            Long id = 1L;
            TenantPackageGrantDTO grantDto = new TenantPackageGrantDTO();
            grantDto.setMenuIds(Set.of(1L));
            TenantPackage entity = new TenantPackage();

            when(packageRepository.findById(id)).thenReturn(Optional.of(entity));
            when(packageRepository.save(entity)).thenReturn(entity);
            when(menuRepository.findAllById(any())).thenReturn(Collections.emptyList());

            tenantPackageService.grant(id, grantDto);

            verify(packageRepository).save(entity);
        }

        @Test
        void updatePackage_NotFound_ThrowsException() {
            when(packageRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.updatePackage(1L, new TenantPackageSaveDTO()));
        }

        @Test
        void updateTenantState_Success() {
            Long id = 1L;
            TenantPackage entity = new TenantPackage();
            when(packageRepository.findById(id)).thenReturn(Optional.of(entity));
            when(packageRepository.save(entity)).thenReturn(entity);
            when(packageMapper.toDto(entity)).thenReturn(new TenantPackageDTO());

            tenantPackageService.updateTenantState(id, true);

            verify(packageRepository).save(entity);
            assertTrue(entity.getEnabled());
        }
    }

    @Nested
    @DisplayName("删除测试")
    class DeleteTests {
        @Test
        void deleteByVerify_Success() {
            Long id = 1L;
            TenantPackage entity = new TenantPackage();
            when(packageRepository.findById(id)).thenReturn(Optional.of(entity));
            when(tenantRepository.countByPackageId(id)).thenReturn(0L);

            tenantPackageService.deleteByVerify(id);

            verify(packageRepository).delete(entity);
        }

        @Test
        void deleteByVerify_InUse_ThrowsException() {
            Long id = 1L;
            when(packageRepository.findById(id)).thenReturn(Optional.of(new TenantPackage()));
            when(tenantRepository.countByPackageId(id)).thenReturn(1L);

            BizException ex = assertThrows(BizException.class, () -> tenantPackageService.deleteByVerify(id));
            assertEquals("套餐正在使用中，不允许删除", ex.getMessage());
        }

        @Test
        void deleteByVerify_NotFound_ThrowsException() {
            when(packageRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.deleteByVerify(1L));
        }
    }
}
