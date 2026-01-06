package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageGrantDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageSaveDTO;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.application.sys.mapper.PermissionMapper;
import com.mok.ddd.application.sys.mapper.TenantPackageMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.model.TenantPackage;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.domain.sys.repository.TenantPackageRepository;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private MockedStatic<TenantPackage> mockedTenantPackage;

    @BeforeEach
    void setUp() {
        mockedTenantPackage = mockStatic(TenantPackage.class);
    }

    @AfterEach
    void tearDown() {
        mockedTenantPackage.close();
    }

    @Nested
    @DisplayName("Write Operations")
    class WriteTests {
        @Test
        void createPackage_Success() {
            TenantPackageSaveDTO saveDto = new TenantPackageSaveDTO();
            saveDto.setName("Test Package");
            saveDto.setDescription("Desc");
            TenantPackage mockEntity = mock(TenantPackage.class);

            mockedTenantPackage.when(() -> TenantPackage.create(saveDto.getName(), saveDto.getDescription())).thenReturn(mockEntity);

            tenantPackageService.createPackage(saveDto);

            verify(packageRepository).save(mockEntity);
        }

        @Test
        void updatePackage_Success() {
            Long id = 1L;
            TenantPackageSaveDTO saveDto = new TenantPackageSaveDTO();
            saveDto.setName("New Name");
            saveDto.setDescription("New Desc");
            TenantPackage mockEntity = mock(TenantPackage.class);

            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));

            tenantPackageService.updatePackage(id, saveDto);

            verify(mockEntity).updateInfo(saveDto.getName(), saveDto.getDescription());
            verify(packageRepository).save(mockEntity);
        }

        @Test
        void grant_Success() {
            Long id = 1L;
            TenantPackageGrantDTO grantDto = new TenantPackageGrantDTO();
            grantDto.setMenuIds(Set.of(1L));
            grantDto.setPermissionIds(Set.of(100L));
            TenantPackage mockEntity = mock(TenantPackage.class);
            Set<Menu> menus = Set.of(mock(Menu.class));
            Set<Permission> permissions = Set.of(mock(Permission.class));

            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.findAllById(grantDto.getMenuIds())).thenReturn(List.copyOf(menus));
            when(permissionRepository.findAllById(grantDto.getPermissionIds())).thenReturn(List.copyOf(permissions));

            tenantPackageService.grant(id, grantDto);

            verify(mockEntity).changeMenus(new HashSet<>(menus));
            verify(mockEntity).changePermissions(new HashSet<>(permissions));
            verify(packageRepository).save(mockEntity);
        }

        @Test
        void updateTenantState_ToNormal() {
            Long id = 1L;
            TenantPackage mockEntity = mock(TenantPackage.class);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(packageRepository.save(mockEntity)).thenReturn(mockEntity);

            tenantPackageService.updateTenantState(id, Const.TenantPackageState.NORMAL);

            verify(mockEntity).enable();
            verify(packageRepository).save(mockEntity);
        }

        @Test
        void updateTenantState_ToDisabled() {
            Long id = 1L;
            TenantPackage mockEntity = mock(TenantPackage.class);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(packageRepository.save(mockEntity)).thenReturn(mockEntity);

            tenantPackageService.updateTenantState(id, Const.TenantPackageState.DISABLED);

            verify(mockEntity).disable();
            verify(packageRepository).save(mockEntity);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {
        @Test
        void deleteByVerify_Success() {
            Long id = 1L;
            TenantPackage mockEntity = mock(TenantPackage.class);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(tenantRepository.countByPackageId(id)).thenReturn(0L);

            tenantPackageService.deleteByVerify(id);

            verify(packageRepository).delete(mockEntity);
        }

        @Test
        void deleteByVerify_InUse_ThrowsException() {
            Long id = 1L;
            when(packageRepository.findById(id)).thenReturn(Optional.of(mock(TenantPackage.class)));
            when(tenantRepository.countByPackageId(id)).thenReturn(1L);

            assertThrows(BizException.class, () -> tenantPackageService.deleteByVerify(id));
        }
    }
}
