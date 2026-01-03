package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.dict.DictDataDTO;
import com.mok.ddd.application.sys.dto.dict.DictDataSaveDTO;
import com.mok.ddd.application.sys.dto.dict.DictTypeDTO;
import com.mok.ddd.application.sys.dto.dict.DictTypeSaveDTO;
import com.mok.ddd.application.sys.mapper.DictDataMapper;
import com.mok.ddd.application.sys.mapper.DictTypeMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.DictData;
import com.mok.ddd.domain.sys.model.DictType;
import com.mok.ddd.domain.sys.repository.DictDataRepository;
import com.mok.ddd.domain.sys.repository.DictTypeRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DictService 单元测试")
class DictServiceTest {

    @InjectMocks
    private DictService dictService;

    @Mock
    private DictTypeRepository dictTypeRepository;

    @Mock
    private DictDataRepository dictDataRepository;

    @Mock
    private DictTypeMapper dictTypeMapper;

    @Mock
    private DictDataMapper dictDataMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Nested
    @DisplayName("DictType 字典类型管理测试")
    class DictTypeTests {

        @Test
        @DisplayName("创建类型成功")
        void createType_Success() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setCode("test_code");
            dto.setName("Test Dict");

            DictType entity = new DictType();
            entity.setCode("test_code");
            
            DictType savedEntity = new DictType();
            savedEntity.setId(1L);
            savedEntity.setCode("test_code");
            savedEntity.setIsSystem(false);

            when(dictTypeRepository.existsByCode(dto.getCode())).thenReturn(false);
            when(dictTypeMapper.toEntity(dto)).thenReturn(entity);
            when(dictTypeRepository.save(entity)).thenReturn(savedEntity);
            when(dictTypeMapper.toDto(savedEntity)).thenReturn(new DictTypeDTO());

            DictTypeDTO result = dictService.createType(dto);

            assertNotNull(result);
            assertFalse(entity.getIsSystem());
            verify(dictTypeRepository).save(entity);
        }

        @Test
        @DisplayName("创建类型失败：编码已存在")
        void createType_DuplicateCode_ThrowsException() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setCode("existing_code");

            when(dictTypeRepository.existsByCode(dto.getCode())).thenReturn(true);

            assertThrows(BizException.class, () -> dictService.createType(dto));
            verify(dictTypeRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新类型成功：普通字典")
        void updateType_Normal_Success() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setId(1L);
            dto.setCode("new_code");

            DictType existing = new DictType();
            existing.setId(1L);
            existing.setCode("old_code");
            existing.setIsSystem(false);

            when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(dictTypeRepository.save(existing)).thenReturn(existing);
            when(dictTypeMapper.toDto(existing)).thenReturn(new DictTypeDTO());

            dictService.updateType(dto);

            verify(dictTypeMapper).updateEntityFromDto(dto, existing);
            verify(dictTypeRepository).save(existing);
        }

        @Test
        @DisplayName("更新类型失败：系统内置字典修改编码")
        void updateType_System_ModifyCode_ThrowsException() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setId(1L);
            dto.setCode("new_code");

            DictType existing = new DictType();
            existing.setId(1L);
            existing.setCode("sys_code");
            existing.setIsSystem(true);

            when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(existing));

            BizException ex = assertThrows(BizException.class, () -> dictService.updateType(dto));
            assertEquals("系统内置字典禁止修改编码", ex.getMessage());
            verify(dictTypeRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新类型成功：系统内置字典仅修改名称")
        void updateType_System_ModifyName_Success() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setId(1L);
            dto.setCode("sys_code");
            dto.setName("New Name");

            DictType existing = new DictType();
            existing.setId(1L);
            existing.setCode("sys_code");
            existing.setIsSystem(true);

            when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(dictTypeRepository.save(existing)).thenReturn(existing);
            when(dictTypeMapper.toDto(existing)).thenReturn(new DictTypeDTO());

            dictService.updateType(dto);

            verify(dictTypeMapper).updateEntityFromDto(dto, existing);
            verify(dictTypeRepository).save(existing);
        }

        @Test
        @DisplayName("删除类型成功")
        void deleteType_Success() {
            Long id = 1L;
            DictType existing = new DictType();
            existing.setId(id);
            existing.setCode("test_code");
            existing.setIsSystem(false);

            when(dictTypeRepository.findById(id)).thenReturn(Optional.of(existing));

            dictService.deleteType(id);

            verify(dictDataRepository).deleteByTypeCode("test_code");
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "test_code");
            verify(dictTypeRepository).delete(existing);
        }

        @Test
        @DisplayName("删除类型失败：系统内置字典")
        void deleteType_System_ThrowsException() {
            Long id = 1L;
            DictType existing = new DictType();
            existing.setIsSystem(true);

            when(dictTypeRepository.findById(id)).thenReturn(Optional.of(existing));

            assertThrows(BizException.class, () -> dictService.deleteType(id));
            verify(dictTypeRepository, never()).delete((DictType) any());
        }
        
        @Test
        @DisplayName("分页查询成功")
        void findPage_Success() {
            com.querydsl.core.types.Predicate predicate = mock(com.querydsl.core.types.Predicate.class);
            org.springframework.data.domain.Pageable pageable = mock(org.springframework.data.domain.Pageable.class);
            
            // Mock Repository findAll
            Page<DictType> page = new PageImpl<>(Collections.singletonList(new DictType()));
            when(dictTypeRepository.findAll(any(com.querydsl.core.types.Predicate.class), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            Page<@NonNull DictTypeDTO> result = dictService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("DictData 字典数据管理测试")
    class DictDataTests {

        @Test
        @DisplayName("创建数据成功")
        void createData_Success() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setTypeCode("test_code");

            DictType type = new DictType();
            type.setIsSystem(false);

            DictData entity = new DictData();
            entity.setTypeCode("test_code");

            when(dictTypeRepository.findByCode("test_code")).thenReturn(Optional.of(type));
            when(dictDataMapper.toEntity(dto)).thenReturn(entity);
            when(dictDataRepository.save(entity)).thenReturn(entity);
            when(dictDataMapper.toDto(entity)).thenReturn(new DictDataDTO());

            dictService.createData(dto);

            verify(dictDataRepository).save(entity);
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "test_code");
        }

        @Test
        @DisplayName("创建数据失败：系统内置字典")
        void createData_System_ThrowsException() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setTypeCode("sys_code");

            DictType type = new DictType();
            type.setIsSystem(true);

            when(dictTypeRepository.findByCode("sys_code")).thenReturn(Optional.of(type));

            assertThrows(BizException.class, () -> dictService.createData(dto));
            verify(dictDataRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新数据成功：修改了TypeCode")
        void updateData_ChangeTypeCode_Success() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setId(1L);
            dto.setTypeCode("new_code");

            DictData existing = new DictData();
            existing.setId(1L);
            existing.setTypeCode("old_code");

            DictType oldType = new DictType();
            oldType.setIsSystem(false);

            when(dictDataRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(dictTypeRepository.findByCode("old_code")).thenReturn(Optional.of(oldType));
            
            // 关键修正：模拟 updateEntityFromDto 真正修改对象
            doAnswer(invocation -> {
                DictDataSaveDTO source = invocation.getArgument(0);
                DictData target = invocation.getArgument(1);
                target.setTypeCode(source.getTypeCode());
                return null;
            }).when(dictDataMapper).updateEntityFromDto(dto, existing);

            when(dictDataRepository.save(existing)).thenReturn(existing);
            when(dictDataMapper.toDto(existing)).thenReturn(new DictDataDTO());

            dictService.updateData(dto);

            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "old_code");
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "new_code");
        }

        @Test
        @DisplayName("删除数据成功")
        void deleteData_Success() {
            Long id = 1L;
            DictData existing = new DictData();
            existing.setId(id);
            existing.setTypeCode("test_code");

            DictType type = new DictType();
            type.setIsSystem(false);

            when(dictDataRepository.findById(id)).thenReturn(Optional.of(existing));
            when(dictTypeRepository.findByCode("test_code")).thenReturn(Optional.of(type));

            dictService.deleteData(id);

            verify(dictDataRepository).delete(existing);
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "test_code");
        }
    }
}
