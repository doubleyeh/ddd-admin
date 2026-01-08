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
        @DisplayName("创建权限成功 - 不关联菜单")
        void createPermission_WithoutMenu_Success() {
            PermissionDTO dto = new PermissionDTO();
            dto.setMenuId(null);
            Permission mockEntity = mock(Permission.class);

            mockedPermission.when(() -> Permission.create(any(), any(), any(), any(), any(), isNull())).thenReturn(mockEntity);
            when(permissionRepository.save(mockEntity)).thenReturn(mockEntity);

            permissionService.createPermission(dto);

            verify(permissionRepository).save(mockEntity);
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
        @DisplayName("更新权限失败 - 权限不存在")
        void updatePermission_NotFound_ThrowsException() {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(1L);
            when(permissionRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> permissionService.updatePermission(dto));
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
}
