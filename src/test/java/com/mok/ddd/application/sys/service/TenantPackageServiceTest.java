package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageGrantDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageOptionDTO;
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
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
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
        void updatePackage_NotFound_ThrowsException() {
            Long id = 1L;
            TenantPackageSaveDTO saveDto = new TenantPackageSaveDTO();
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.updatePackage(id, saveDto));
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

            when(redisTemplate.delete(any(Set.class))).thenReturn(1L);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.findAllById(grantDto.getMenuIds())).thenReturn(List.copyOf(menus));
            when(permissionRepository.findAllById(grantDto.getPermissionIds())).thenReturn(List.copyOf(permissions));

            tenantPackageService.grant(id, grantDto);

            verify(mockEntity).changeMenus(new HashSet<>(menus));
            verify(mockEntity).changePermissions(new HashSet<>(permissions));
            verify(packageRepository).save(mockEntity);
            verify(redisTemplate).delete(any(Set.class));
        }
        
        @Test
        void grant_NullIds_Success() {
            Long id = 1L;
            TenantPackageGrantDTO grantDto = new TenantPackageGrantDTO();
            grantDto.setMenuIds(null);
            grantDto.setPermissionIds(null);
            TenantPackage mockEntity = mock(TenantPackage.class);

            when(redisTemplate.delete(any(Set.class))).thenReturn(1L);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));

            tenantPackageService.grant(id, grantDto);

            verify(mockEntity, never()).changeMenus(any());
            verify(mockEntity, never()).changePermissions(any());
            verify(packageRepository).save(mockEntity);
            verify(redisTemplate).delete(any(Set.class));
        }
        
        @Test
        void grant_NotFound_ThrowsException() {
            Long id = 1L;
            TenantPackageGrantDTO grantDto = new TenantPackageGrantDTO();
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.grant(id, grantDto));
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
        
        @Test
        void updateTenantState_NotFound_ThrowsException() {
            Long id = 1L;
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.updateTenantState(id, Const.TenantPackageState.NORMAL));
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
        
        @Test
        void deleteByVerify_NotFound_ThrowsException() {
            Long id = 1L;
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.deleteByVerify(id));
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadTests {

        @Test
        void findOptions_Success() {
            String name = "test";
            List<TenantPackage> packages = List.of(mock(TenantPackage.class));
            List<TenantPackageDTO> dtos = List.of(new TenantPackageDTO());
            List<TenantPackageOptionDTO> options = List.of(new TenantPackageOptionDTO());

            when(packageRepository.findAll(any(Predicate.class))).thenReturn(packages);
            when(packageMapper.toDto(any(TenantPackage.class))).thenReturn(new TenantPackageDTO());
            when(packageMapper.dtoToOptionsDto(anyList())).thenReturn(options);

            List<TenantPackageOptionDTO> result = tenantPackageService.findOptions(name);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
        
        @Test
        void findOptions_NullName_Success() {
            List<TenantPackage> packages = List.of(mock(TenantPackage.class));
            List<TenantPackageOptionDTO> options = List.of(new TenantPackageOptionDTO());

            when(packageRepository.findAll(any(Predicate.class))).thenReturn(packages);
            when(packageMapper.toDto(any(TenantPackage.class))).thenReturn(new TenantPackageDTO());
            when(packageMapper.dtoToOptionsDto(anyList())).thenReturn(options);

            List<TenantPackageOptionDTO> result = tenantPackageService.findOptions(null);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void getMenuIdsByPackage_CacheHit() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            Long id = 1L;
            String key = Const.CacheKey.TENANT_PACKAGE_PERMS + ":menus:" + id;
            Set<Long> cachedIds = Set.of(10L, 20L);

            when(valueOperations.get(key)).thenReturn(cachedIds);

            Set<Long> result = tenantPackageService.getMenuIdsByPackage(id);

            assertEquals(cachedIds, result);
            verify(packageRepository, never()).findById(any());
        }

        @Test
        void getMenuIdsByPackage_CacheMiss() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            Long id = 1L;
            String key = Const.CacheKey.TENANT_PACKAGE_PERMS + ":menus:" + id;
            TenantPackage mockEntity = mock(TenantPackage.class);
            Menu menu = mock(Menu.class);
            when(menu.getId()).thenReturn(10L);
            when(mockEntity.getMenus()).thenReturn(Set.of(menu));

            when(valueOperations.get(key)).thenReturn(null);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));

            Set<Long> result = tenantPackageService.getMenuIdsByPackage(id);

            assertEquals(Set.of(10L), result);
            verify(valueOperations).set(key, Set.of(10L));
        }
        
        @Test
        void getMenuIdsByPackage_NotFoundOrNullMenus() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            Long id = 1L;
            when(valueOperations.get(anyString())).thenReturn(null);
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertTrue(tenantPackageService.getMenuIdsByPackage(id).isEmpty());

            TenantPackage mockEntity = mock(TenantPackage.class);
            when(mockEntity.getMenus()).thenReturn(null);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            assertTrue(tenantPackageService.getMenuIdsByPackage(id).isEmpty());
        }

        @Test
        void getPermissionIdsByPackage_CacheHit() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            Long id = 1L;
            String key = Const.CacheKey.TENANT_PACKAGE_PERMS + ":permissions:" + id;
            Set<Long> cachedIds = Set.of(100L, 200L);

            when(valueOperations.get(key)).thenReturn(cachedIds);

            Set<Long> result = tenantPackageService.getPermissionIdsByPackage(id);

            assertEquals(cachedIds, result);
            verify(packageRepository, never()).findById(any());
        }

        @Test
        void getPermissionIdsByPackage_CacheMiss() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            Long id = 1L;
            String key = Const.CacheKey.TENANT_PACKAGE_PERMS + ":permissions:" + id;
            TenantPackage mockEntity = mock(TenantPackage.class);
            Permission permission = mock(Permission.class);
            when(permission.getId()).thenReturn(100L);
            when(mockEntity.getPermissions()).thenReturn(Set.of(permission));

            when(valueOperations.get(key)).thenReturn(null);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));

            Set<Long> result = tenantPackageService.getPermissionIdsByPackage(id);

            assertEquals(Set.of(100L), result);
            verify(valueOperations).set(key, Set.of(100L));
        }
        
        @Test
        void getPermissionIdsByPackage_NotFoundOrNullPermissions() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            Long id = 1L;
            when(valueOperations.get(anyString())).thenReturn(null);
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertTrue(tenantPackageService.getPermissionIdsByPackage(id).isEmpty());

            TenantPackage mockEntity = mock(TenantPackage.class);
            when(mockEntity.getPermissions()).thenReturn(null);
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            assertTrue(tenantPackageService.getPermissionIdsByPackage(id).isEmpty());
        }

        @Test
        void getById_Success() {
            Long id = 1L;
            TenantPackage mockEntity = mock(TenantPackage.class);
            TenantPackageDTO mockDto = new TenantPackageDTO();
            
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(packageMapper.toDto(mockEntity)).thenReturn(mockDto);
            
            when(mockEntity.getMenus()).thenReturn(Set.of(mock(Menu.class)));
            when(mockEntity.getPermissions()).thenReturn(Set.of(mock(Permission.class)));

            TenantPackageDTO result = tenantPackageService.getById(id);

            assertNotNull(result);
            assertNotNull(result.getMenus());
            assertNotNull(result.getPermissions());
        }
        
        @Test
        void getById_NullRelations() {
            Long id = 1L;
            TenantPackage mockEntity = mock(TenantPackage.class);
            TenantPackageDTO mockDto = new TenantPackageDTO();
            
            when(packageRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(packageMapper.toDto(mockEntity)).thenReturn(mockDto);
            
            when(mockEntity.getMenus()).thenReturn(null);
            when(mockEntity.getPermissions()).thenReturn(null);

            TenantPackageDTO result = tenantPackageService.getById(id);

            assertNotNull(result);
            assertTrue(result.getMenus().isEmpty());
            assertTrue(result.getPermissions().isEmpty());
        }
        
        @Test
        void getById_NotFound_ThrowsException() {
            Long id = 1L;
            when(packageRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(BizException.class, () -> tenantPackageService.getById(id));
        }
    }
}
