package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.menu.MenuOptionDTO;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.model.Tenant;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private MenuMapper menuMapper;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private TenantPackageService tenantPackageService;

    private MockedStatic<Menu> mockedMenu;
    private MockedStatic<TenantContextHolder> mockedTenantContext;
    private MockedStatic<SysUtil> mockedSysUtil;

    @BeforeEach
    void setUp() {
        mockedMenu = mockStatic(Menu.class);
        mockedTenantContext = mockStatic(TenantContextHolder.class);
        mockedSysUtil = mockStatic(SysUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedMenu.close();
        mockedTenantContext.close();
        mockedSysUtil.close();
    }

    @Nested
    @DisplayName("Write Operations")
    class WriteTests {
        @Test
        void createMenu_Success() {
            MenuDTO dto = new MenuDTO();
            dto.setParentId(1L);
            Menu parent = mock(Menu.class);
            Menu mockEntity = mock(Menu.class);
            when(menuRepository.findById(1L)).thenReturn(Optional.of(parent));
            mockedMenu.when(() -> Menu.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockEntity);
            when(menuRepository.save(mockEntity)).thenReturn(mockEntity);

            menuService.createMenu(dto);
            verify(menuRepository).save(mockEntity);
        }

        @Test
        void updateMenu_Success() {
            MenuDTO dto = new MenuDTO();
            dto.setId(1L);
            Menu mockEntity = mock(Menu.class);
            when(menuRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.save(mockEntity)).thenReturn(mockEntity);

            menuService.updateMenu(dto);
            verify(mockEntity).updateInfo(any(), any(), any(), any(), any(), any(), any());
            verify(menuRepository).save(mockEntity);
        }

        @Test
        void changePermissions_Success() {
            Long menuId = 1L;
            Set<Long> permissionIds = Set.of(100L);
            Menu mockMenu = mock(Menu.class);
            Permission mockPermission = mock(Permission.class);
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(mockMenu));
            when(permissionRepository.findAllById(permissionIds)).thenReturn(List.of(mockPermission));

            menuService.changePermissions(menuId, permissionIds);

            verify(mockMenu).changePermissions(eq(new HashSet<>(List.of(mockPermission))));
            verify(menuRepository).save(mockMenu);
        }

        @Test
        void deleteById_Success() {
            Long menuId = 1L;
            Menu childMenu = mock(Menu.class);
            when(childMenu.getId()).thenReturn(2L);
            when(menuRepository.findByParentId(menuId)).thenReturn(List.of(childMenu));
            when(menuRepository.findByParentId(2L)).thenReturn(Collections.emptyList());
            when(menuRepository.findRoleIdsByMenuIds(anyList())).thenReturn(List.of(10L));

            menuService.deleteById(menuId);

            verify(permissionRepository).deleteRolePermissionsByMenuIds(anyList());
            verify(permissionRepository).deleteByMenuIds(anyList());
            verify(menuRepository).deleteRoleMenuByMenuIds(anyList());
            verify(menuRepository).deleteAllById(anyList());
            verify(redisTemplate).delete(Const.CacheKey.MENU_TREE);
            verify(redisTemplate).delete(anyList());
        }
    }

    @Nested
    @DisplayName("Build Tree Operations")
    class BuildTreeTests {
        @Test
        void buildMenuTree_Success() {
            MenuDTO root = new MenuDTO();
            root.setId(1L);
            root.setPath("/root"); // Add path to prevent filtering

            MenuDTO child = new MenuDTO();
            child.setId(2L);
            child.setParentId(1L);
            child.setPath("/child");
            
            List<MenuDTO> flatList = List.of(root, child);

            List<MenuDTO> tree = menuService.buildMenuTree(flatList);

            assertEquals(1, tree.size());
            assertEquals(1, tree.getFirst().getChildren().size());
            assertEquals(child, tree.getFirst().getChildren().getFirst());
        }

        @Test
        void buildMenuAndPermissionTree_SuperAdmin() {
            mockedSysUtil.when(() -> SysUtil.isSuperTenant(any())).thenReturn(true);
            when(menuRepository.findAll()).thenReturn(List.of(mock(Menu.class)));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertNotNull(result);
        }

        @Test
        void buildMenuAndPermissionTree_NormalTenant() {
            mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("tenant1");
            mockedSysUtil.when(() -> SysUtil.isSuperTenant("tenant1")).thenReturn(false);
            
            Tenant mockTenant = mock(Tenant.class);
            when(mockTenant.getPackageId()).thenReturn(1L);
            when(tenantRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mockTenant));
            
            when(tenantPackageService.getMenuIdsByPackage(1L)).thenReturn(Set.of(1L));
            when(tenantPackageService.getPermissionIdsByPackage(1L)).thenReturn(Set.of(100L));
            
            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            Permission mockPermission = mock(Permission.class);
            when(mockPermission.getId()).thenReturn(100L);
            when(mockMenu.getPermissions()).thenReturn(Set.of(mockPermission));
            when(menuRepository.findAll()).thenReturn(List.of(mockMenu));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
}
