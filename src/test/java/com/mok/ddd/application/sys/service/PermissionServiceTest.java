package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import com.mok.ddd.application.sys.mapper.PermissionMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService 单元测试")
class PermissionServiceTest {

    @InjectMocks
    private PermissionService permissionService;

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ListOperations<String, String> listOperations;

    private MockedStatic<Permission> mockedPermission;

    @BeforeEach
    void setUp() {
        mockedPermission = mockStatic(Permission.class);
    }

    @AfterEach
    void tearDown() {
        mockedPermission.close();
    }

    @Nested
    @DisplayName("createPermission")
    class CreatePermissionTests {
        @Test
        @DisplayName("创建权限成功 - 关联菜单")
        void createPermission_WithMenu_Success() {
            PermissionDTO dto = new PermissionDTO();
            dto.setMenuId(1L);
            Menu menu = mock(Menu.class);
            Permission mockEntity = mock(Permission.class);
            PermissionDTO mockResultDTO = new PermissionDTO();

            when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
            mockedPermission.when(() -> Permission.create(any(), any(), any(), any(), any(), eq(menu))).thenReturn(mockEntity);
            when(permissionRepository.save(mockEntity)).thenReturn(mockEntity);
            when(permissionMapper.toDto(mockEntity)).thenReturn(mockResultDTO);

            PermissionDTO result = permissionService.createPermission(dto);

            assertSame(mockResultDTO, result);
            verify(permissionRepository).save(mockEntity);
        }

        @Test
        @DisplayName("创建权限成功 - 关联菜单但菜单不存在")
        void createPermission_WithNonExistentMenu_Success() {
            PermissionDTO dto = new PermissionDTO();
            dto.setMenuId(1L);
            Permission mockEntity = mock(Permission.class);

            when(menuRepository.findById(1L)).thenReturn(Optional.empty());
            mockedPermission.when(() -> Permission.create(any(), any(), any(), any(), any(), isNull())).thenReturn(mockEntity);
            when(permissionRepository.save(mockEntity)).thenReturn(mockEntity);

            permissionService.createPermission(dto);

            verify(permissionRepository).save(mockEntity);
            // 验证创建时传入的 menu 是 null
            mockedPermission.verify(() -> Permission.create(any(), any(), any(), any(), any(), isNull()));
        }
    }

    @Nested
    @DisplayName("updatePermission")
    class UpdatePermissionTests {
        @Test
        @DisplayName("更新权限成功")
        void updatePermission_Success() {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(1L);
            dto.setMenuId(2L);
            Permission mockEntity = mock(Permission.class);
            Menu newMenu = mock(Menu.class);

            when(permissionRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.findById(2L)).thenReturn(Optional.of(newMenu));

            permissionService.updatePermission(dto);

            verify(mockEntity).updateInfo(any(), any(), any(), any(), any(), eq(newMenu));
            verify(permissionRepository).save(mockEntity);
        }

        @Test
        @DisplayName("更新权限成功 - 不关联菜单")
        void updatePermission_WithoutMenu_Success() {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(1L);
            dto.setMenuId(null);
            Permission mockEntity = mock(Permission.class);

            when(permissionRepository.findById(1L)).thenReturn(Optional.of(mockEntity));

            permissionService.updatePermission(dto);

            verify(mockEntity).updateInfo(any(), any(), any(), any(), any(), isNull());
            verify(permissionRepository).save(mockEntity);
            verify(menuRepository, never()).findById(any());
        }

        @Test
        @DisplayName("更新权限成功 - 关联菜单但菜单不存在")
        void updatePermission_WithNonExistentMenu_Success() {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(1L);
            dto.setMenuId(2L);
            Permission mockEntity = mock(Permission.class);

            when(permissionRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(menuRepository.findById(2L)).thenReturn(Optional.empty());

            permissionService.updatePermission(dto);

            verify(mockEntity).updateInfo(any(), any(), any(), any(), any(), isNull());
            verify(permissionRepository).save(mockEntity);
        }

        @Test
        @DisplayName("更新权限失败 - 权限不存在")
        void updatePermission_NotFound_ThrowsException() {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(1L);
            when(permissionRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> permissionService.updatePermission(dto));
        }
    }

    @Nested
    @DisplayName("getAllPermissionCodes")
    class GetAllPermissionCodesTests {

        @Test
        @DisplayName("获取所有权限代码 - 存在权限")
        void getAllPermissionCodes_WithPermissions_ReturnsCodes() {
            Permission p1 = mock(Permission.class);
            when(p1.getCode()).thenReturn("perm1");
            Permission p2 = mock(Permission.class);
            when(p2.getCode()).thenReturn("perm2");

            when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));

            Set<String> codes = permissionService.getAllPermissionCodes();

            assertEquals(2, codes.size());
            assertTrue(codes.containsAll(Set.of("perm1", "perm2")));
        }

        @Test
        @DisplayName("获取所有权限代码 - 不存在权限")
        void getAllPermissionCodes_NoPermissions_ReturnsEmptySet() {
            when(permissionRepository.findAll()).thenReturn(Collections.emptyList());

            Set<String> codes = permissionService.getAllPermissionCodes();

            assertTrue(codes.isEmpty());
        }
    }


    @Nested
    @DisplayName("getPermissionsByRoleIds")
    class GetPermissionsByRoleIdsTests {

        @Test
        @DisplayName("通过角色ID获取权限 - 缓存命中")
        void getPermissionsByRoleIds_CacheHit() {
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            Set<Long> roleIds = Set.of(1L);
            String cacheKey = Const.CacheKey.ROLE_PERMS + ":1";
            when(listOperations.range(cacheKey, 0, -1)).thenReturn(List.of("perm1", "perm2"));

            Set<String> result = permissionService.getPermissionsByRoleIds(roleIds);

            assertEquals(2, result.size());
            assertTrue(result.contains("perm1"));
            verify(permissionRepository, never()).findCodesByRoleId(anyLong());
        }

        @Test
        @DisplayName("通过角色ID获取权限 - 缓存未命中，数据库命中")
        void getPermissionsByRoleIds_CacheMiss_DbHit() {
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            Set<Long> roleIds = Set.of(1L);
            String cacheKey = Const.CacheKey.ROLE_PERMS + ":1";
            when(listOperations.range(cacheKey, 0, -1)).thenReturn(Collections.emptyList());
            when(permissionRepository.findCodesByRoleId(1L)).thenReturn(List.of("perm1_db"));

            Set<String> result = permissionService.getPermissionsByRoleIds(roleIds);

            assertEquals(1, result.size());
            assertTrue(result.contains("perm1_db"));
            verify(listOperations).rightPushAll(cacheKey, List.of("perm1_db"));
        }

        @Test
        @DisplayName("通过角色ID获取权限 - 多个角色ID，部分缓存命中，部分DB命中")
        void getPermissionsByRoleIds_MultipleIds_MixedHit() {
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            Set<Long> roleIds = Set.of(1L, 2L, 3L, 4L);
            String cacheKey1 = Const.CacheKey.ROLE_PERMS + ":1";
            String cacheKey2 = Const.CacheKey.ROLE_PERMS + ":2";
            String cacheKey3 = Const.CacheKey.ROLE_PERMS + ":3";
            String cacheKey4 = Const.CacheKey.ROLE_PERMS + ":4";

            // Role 1: Cache hit
            when(listOperations.range(cacheKey1, 0, -1)).thenReturn(List.of("perm_cache1", "perm_common"));
            // Role 2: Cache miss, DB hit
            when(listOperations.range(cacheKey2, 0, -1)).thenReturn(Collections.emptyList());
            when(permissionRepository.findCodesByRoleId(2L)).thenReturn(List.of("perm_db2", "perm_common"));
            // Role 3: Cache miss, DB miss
            when(listOperations.range(cacheKey3, 0, -1)).thenReturn(Collections.emptyList());
            when(permissionRepository.findCodesByRoleId(3L)).thenReturn(Collections.emptyList());
             // Role 4: Cache returns null
            when(listOperations.range(cacheKey4, 0, -1)).thenReturn(null);
            when(permissionRepository.findCodesByRoleId(4L)).thenReturn(List.of("perm_db4"));


            Set<String> result = permissionService.getPermissionsByRoleIds(roleIds);

            assertEquals(4, result.size());
            assertTrue(result.containsAll(Set.of("perm_cache1", "perm_common", "perm_db2", "perm_db4")));
            // 验证DB查询被调用的次数
            verify(permissionRepository, times(1)).findCodesByRoleId(2L);
            verify(permissionRepository, times(1)).findCodesByRoleId(3L);
            verify(permissionRepository, times(1)).findCodesByRoleId(4L);
            verify(permissionRepository, never()).findCodesByRoleId(1L);
            // 验证缓存被写入的次数
            verify(listOperations, times(1)).rightPushAll(cacheKey2, List.of("perm_db2", "perm_common"));
            verify(listOperations, times(1)).rightPushAll(cacheKey4, List.of("perm_db4"));
            verify(listOperations, never()).rightPushAll(eq(cacheKey3), anyList());
        }

        @Test
        @DisplayName("通过角色ID获取权限 - 缓存和数据库均未命中")
        void getPermissionsByRoleIds_CacheMiss_DbMiss() {
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            Set<Long> roleIds = Set.of(1L);
            String cacheKey = Const.CacheKey.ROLE_PERMS + ":1";
            when(listOperations.range(cacheKey, 0, -1)).thenReturn(Collections.emptyList());
            when(permissionRepository.findCodesByRoleId(1L)).thenReturn(Collections.emptyList());

            Set<String> result = permissionService.getPermissionsByRoleIds(roleIds);

            assertTrue(result.isEmpty());
            verify(listOperations, never()).rightPushAll(anyString(), anyList());
        }

        @Test
        @DisplayName("通过角色ID获取权限 - roleIds为空")
        void getPermissionsByRoleIds_EmptyRoleIds() {
            Set<String> result = permissionService.getPermissionsByRoleIds(Collections.emptySet());
            assertTrue(result.isEmpty());
            // 验证没有与 Redis 交互
            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("通过角色ID获取权限 - roleIds为null")
        void getPermissionsByRoleIds_NullRoleIds() {
            Set<String> result = permissionService.getPermissionsByRoleIds(null);
            assertTrue(result.isEmpty());
            verifyNoInteractions(redisTemplate);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {
        @Test
        @DisplayName("删除权限成功 - 清理缓存")
        void deleteById_Success_ClearsCache() {
            Long permissionId = 1L;
            List<Long> roleIds = List.of(10L, 20L);
            when(permissionRepository.findRoleIdsByPermissionId(permissionId)).thenReturn(roleIds);

            permissionService.deleteById(permissionId);

            verify(permissionRepository).deleteRolePermissionsByPermissionId(permissionId);
            verify(permissionRepository).deleteById(permissionId);
            verify(redisTemplate).delete(List.of(Const.CacheKey.ROLE_PERMS + ":10", Const.CacheKey.ROLE_PERMS + ":20"));
        }

        @Test
        @DisplayName("删除权限成功 - 无需清理缓存")
        void deleteById_Success_NoCacheToClear() {
            Long permissionId = 1L;
            when(permissionRepository.findRoleIdsByPermissionId(permissionId)).thenReturn(Collections.emptyList());

            permissionService.deleteById(permissionId);

            verify(redisTemplate, never()).delete(anyList());
        }
    }

    @Test
    void testToDto() {
        Permission entity = mock(Permission.class);
        PermissionDTO dto = new PermissionDTO();
        when(permissionMapper.toDto(entity)).thenReturn(dto);
        assertSame(dto, permissionService.toDto(entity));
    }

    @Test
    void testGetRepository() {
        assertSame(permissionRepository, permissionService.getRepository());
    }
}
