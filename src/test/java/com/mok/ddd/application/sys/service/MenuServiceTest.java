package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.NotFoundException;
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
        void createMenu_Success_WithParent() {
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
        void createMenu_Success_NoParent() {
            MenuDTO dto = new MenuDTO();
            dto.setParentId(null);
            Menu mockEntity = mock(Menu.class);
            mockedMenu.when(() -> Menu.create(isNull(), any(), any(), any(), any(), any(), any())).thenReturn(mockEntity);
            when(menuRepository.save(mockEntity)).thenReturn(mockEntity);

            menuService.createMenu(dto);
            verify(menuRepository).save(mockEntity);
        }

        @Test
        void updateMenu_Success() {
            MenuDTO dto = new MenuDTO();
            dto.setId(1L);
            dto.setParentId(2L);
            Menu mockEntity = mock(Menu.class);
            Menu parent = mock(Menu.class);
            
            when(menuRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.findById(2L)).thenReturn(Optional.of(parent));
            when(menuRepository.save(mockEntity)).thenReturn(mockEntity);

            menuService.updateMenu(dto);
            verify(mockEntity).updateInfo(eq(parent), any(), any(), any(), any(), any(), any());
            verify(menuRepository).save(mockEntity);
        }
        
        @Test
        void updateMenu_Success_NoParent() {
            MenuDTO dto = new MenuDTO();
            dto.setId(1L);
            dto.setParentId(null);
            Menu mockEntity = mock(Menu.class);
            
            when(menuRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.save(mockEntity)).thenReturn(mockEntity);

            menuService.updateMenu(dto);
            verify(mockEntity).updateInfo(isNull(), any(), any(), any(), any(), any(), any());
            verify(menuRepository).save(mockEntity);
        }
        
        @Test
        void updateMenu_NotFound_ThrowsException() {
            MenuDTO dto = new MenuDTO();
            dto.setId(1L);
            when(menuRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> menuService.updateMenu(dto));
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
        void changePermissions_NotFound_ThrowsException() {
            Long menuId = 1L;
            Set<Long> permissionIds = Set.of(100L);
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> menuService.changePermissions(menuId, permissionIds));
        }
        
        @Test
        void changePermissions_EmptyIds_Success() {
            Long menuId = 1L;
            Menu mockMenu = mock(Menu.class);
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(mockMenu));

            menuService.changePermissions(menuId, null);

            verify(mockMenu).changePermissions(eq(Collections.emptySet()));
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
        
        @Test
        void deleteById_NoRoles_Success() {
            Long menuId = 1L;
            when(menuRepository.findByParentId(menuId)).thenReturn(Collections.emptyList());
            when(menuRepository.findRoleIdsByMenuIds(anyList())).thenReturn(Collections.emptyList());

            menuService.deleteById(menuId);

            verify(redisTemplate).delete(Const.CacheKey.MENU_TREE);
            verify(redisTemplate, times(1)).delete(anyString());
        }
    }

    @Nested
    @DisplayName("Build Tree Operations")
    class BuildTreeTests {
        @Test
        void buildMenuTree_Success() {
            MenuDTO root = new MenuDTO();
            root.setId(1L);
            root.setPath("/root");
            root.setChildren(new ArrayList<>());

            MenuDTO child = new MenuDTO();
            child.setId(2L);
            child.setParentId(1L);
            child.setPath("/child");
            
            List<MenuDTO> flatList = List.of(root, child);

            List<MenuDTO> tree = menuService.buildMenuTree(flatList);

            assertEquals(1, tree.size());
            assertEquals(1, tree.get(0).getChildren().size());
            assertEquals(child, tree.get(0).getChildren().get(0));
        }
        
        @Test
        void buildMenuTree_ParentIdZero() {
            MenuDTO root = new MenuDTO();
            root.setId(1L);
            root.setParentId(0L);
            root.setPath("/root");
            
            List<MenuDTO> flatList = List.of(root);
            List<MenuDTO> tree = menuService.buildMenuTree(flatList);
            
            assertEquals(1, tree.size());
            assertEquals(root, tree.get(0));
        }
        
        @Test
        void buildMenuTree_ParentChildrenInitialized() {
            MenuDTO root = new MenuDTO();
            root.setId(1L);
            root.setPath("/root");
            root.setChildren(new ArrayList<>());

            MenuDTO child = new MenuDTO();
            child.setId(2L);
            child.setParentId(1L);
            child.setPath("/child");
            
            List<MenuDTO> flatList = List.of(root, child);

            List<MenuDTO> tree = menuService.buildMenuTree(flatList);

            assertEquals(1, tree.size());
            assertEquals(1, tree.get(0).getChildren().size());
        }
        
        @Test
        void buildMenuTree_HiddenMenu_Skipped() {
            MenuDTO hidden = new MenuDTO();
            hidden.setId(1L);
            hidden.setIsHidden(true);
            
            List<MenuDTO> flatList = List.of(hidden);
            List<MenuDTO> tree = menuService.buildMenuTree(flatList);
            
            assertTrue(tree.isEmpty());
        }
        
        @Test
        void buildMenuTree_OrphanNode_SkippedOrAddedAsRoot() {
            MenuDTO orphan = new MenuDTO();
            orphan.setId(2L);
            orphan.setParentId(999L);
            orphan.setPath("/orphan");
            
            List<MenuDTO> flatList = List.of(orphan);
            List<MenuDTO> tree = menuService.buildMenuTree(flatList);
            
            assertTrue(tree.isEmpty());
        }
        
        @Test
        void filterEmptyParentMenus_Coverage() {
            MenuDTO parent = new MenuDTO();
            parent.setId(1L);
            parent.setChildren(new ArrayList<>());
            
            MenuDTO child = new MenuDTO();
            child.setId(2L);
            child.setPath("/child");
            parent.getChildren().add(child);
            
            MenuDTO leaf = new MenuDTO();
            leaf.setId(3L);
            leaf.setPath("/leaf");
            
            MenuDTO empty = new MenuDTO();
            empty.setId(4L);
            
            MenuDTO emptyList = new MenuDTO();
            emptyList.setId(5L);
            emptyList.setChildren(new ArrayList<>());
            
            MenuDTO emptyPath = new MenuDTO();
            emptyPath.setId(6L);
            emptyPath.setPath("");

            List<MenuDTO> list = List.of(parent, leaf, empty, emptyList, emptyPath);
            
            List<MenuDTO> tree = menuService.buildMenuTree(list);
            
            assertEquals(2, tree.size());
            assertTrue(tree.contains(parent));
            assertTrue(tree.contains(leaf));
        }

        @Test
        void buildMenuAndPermissionTree_SuperAdmin() {
            mockedSysUtil.when(() -> SysUtil.isSuperTenant(any())).thenReturn(true);
            
            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            when(mockMenu.getPath()).thenReturn("/path");
            when(mockMenu.getPermissions()).thenReturn(Set.of(mock(Permission.class)));
            
            when(menuRepository.findAll()).thenReturn(List.of(mockMenu));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertFalse(result.get(0).getChildren().isEmpty());
        }
        
        @Test
        void buildMenuAndPermissionTree_SuperAdmin_WithParent() {
            mockedSysUtil.when(() -> SysUtil.isSuperTenant(any())).thenReturn(true);
            
            Menu parent = mock(Menu.class);
            when(parent.getId()).thenReturn(10L);
            when(parent.getPath()).thenReturn("/parent");
            
            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            when(mockMenu.getPath()).thenReturn("/path");
            when(mockMenu.getParent()).thenReturn(parent);
            
            when(menuRepository.findAll()).thenReturn(List.of(parent, mockMenu));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertFalse(result.isEmpty());
            
            MenuOptionDTO childDto = result.stream().filter(m -> m.getId().equals(1L)).findFirst().orElse(null);
            if (childDto == null) {
                childDto = result.stream()
                        .flatMap(m -> m.getChildren() != null ? m.getChildren().stream() : java.util.stream.Stream.empty())
                        .filter(m -> m.getId().equals(1L))
                        .findFirst().orElse(null);
            }
            
            assertNotNull(childDto);
            assertEquals(10L, childDto.getParentId());
        }
        
        @Test
        void buildMenuAndPermissionTree_VerifyParentIdMapping() {
            mockedSysUtil.when(() -> SysUtil.isSuperTenant(any())).thenReturn(true);
            
            Menu parent = mock(Menu.class);
            when(parent.getId()).thenReturn(99L);
            when(parent.getPath()).thenReturn("/parent");
            
            Menu child = mock(Menu.class);
            when(child.getId()).thenReturn(100L);
            when(child.getPath()).thenReturn("/child");
            when(child.getParent()).thenReturn(parent);
            
            when(menuRepository.findAll()).thenReturn(List.of(parent, child));
            
            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            
            assertEquals(1, result.size());
            MenuOptionDTO parentDto = result.get(0);
            assertEquals(99L, parentDto.getId());
            
            assertNotNull(parentDto.getChildren());
            assertEquals(1, parentDto.getChildren().size());
            
            MenuOptionDTO childDto = parentDto.getChildren().get(0);
            assertEquals(100L, childDto.getId());
            assertEquals(99L, childDto.getParentId());
        }
        
        @Test
        void buildMenuAndPermissionTree_SuperAdmin_NoPermissions() {
            mockedSysUtil.when(() -> SysUtil.isSuperTenant(any())).thenReturn(true);
            
            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            when(mockMenu.getPath()).thenReturn("/path");
            when(mockMenu.getPermissions()).thenReturn(null);
            
            when(menuRepository.findAll()).thenReturn(List.of(mockMenu));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.get(0).getChildren() == null || result.get(0).getChildren().isEmpty());
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
            when(mockMenu.getPath()).thenReturn("/path");
            Permission mockPermission = mock(Permission.class);
            when(mockPermission.getId()).thenReturn(100L);
            when(mockMenu.getPermissions()).thenReturn(Set.of(mockPermission));
            when(menuRepository.findAll()).thenReturn(List.of(mockMenu));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
        
        @Test
        void buildMenuAndPermissionTree_NormalTenant_NoPermissions() {
            mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("tenant1");
            mockedSysUtil.when(() -> SysUtil.isSuperTenant("tenant1")).thenReturn(false);
            
            Tenant mockTenant = mock(Tenant.class);
            when(mockTenant.getPackageId()).thenReturn(1L);
            when(tenantRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mockTenant));
            
            when(tenantPackageService.getMenuIdsByPackage(1L)).thenReturn(Set.of(1L));
            when(tenantPackageService.getPermissionIdsByPackage(1L)).thenReturn(Set.of(100L));
            
            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            when(mockMenu.getPath()).thenReturn("/path");
            when(mockMenu.getPermissions()).thenReturn(null);
            when(menuRepository.findAll()).thenReturn(List.of(mockMenu));

            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
        
        @Test
        void buildMenuAndPermissionTree_NormalTenant_NoPackage() {
            mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("tenant1");
            mockedSysUtil.when(() -> SysUtil.isSuperTenant("tenant1")).thenReturn(false);
            
            Tenant mockTenant = mock(Tenant.class);
            when(mockTenant.getPackageId()).thenReturn(null);
            when(tenantRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mockTenant));
            
            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertTrue(result.isEmpty());
        }
        
        @Test
        void filterEmptyParentMenuOptions_Coverage() {
            MenuOptionDTO parent = new MenuOptionDTO();
            parent.setId(1L);
            parent.setChildren(new ArrayList<>());
            MenuOptionDTO child = new MenuOptionDTO();
            child.setId(2L);
            child.setPath("/child");
            parent.getChildren().add(child);
            
            MenuOptionDTO leaf = new MenuOptionDTO();
            leaf.setId(3L);
            leaf.setPath("/leaf");
            
            MenuOptionDTO button = new MenuOptionDTO();
            button.setId(4L);
            button.setIsPermission(true);
            
            MenuOptionDTO empty = new MenuOptionDTO();
            empty.setId(5L);
            
            MenuOptionDTO parentNoPath = new MenuOptionDTO();
            parentNoPath.setId(6L);
            parentNoPath.setChildren(new ArrayList<>());
            parentNoPath.getChildren().add(leaf);
            
            mockedSysUtil.when(() -> SysUtil.isSuperTenant(any())).thenReturn(true);
            Menu menuWithChildrenNoPath = mock(Menu.class);
            when(menuWithChildrenNoPath.getId()).thenReturn(6L);
            when(menuWithChildrenNoPath.getPath()).thenReturn(null);
            
            Menu menuChild = mock(Menu.class);
            when(menuChild.getId()).thenReturn(7L);
            when(menuChild.getPath()).thenReturn("/child");
            when(menuChild.getParent()).thenReturn(menuWithChildrenNoPath);
            
            when(menuRepository.findAll()).thenReturn(List.of(menuWithChildrenNoPath, menuChild));
            
            List<MenuOptionDTO> result = menuService.buildMenuAndPermissionTree();
            assertEquals(1, result.size());
        }
    }
    
    @Test
    void testProtectedMethods() {
        assertEquals(menuRepository, menuService.getRepository());
        
        Menu menu = mock(Menu.class);
        MenuDTO dto = new MenuDTO();
        when(menuMapper.toDto(menu)).thenReturn(dto);
        assertEquals(dto, menuService.toDto(menu));
    }
}
