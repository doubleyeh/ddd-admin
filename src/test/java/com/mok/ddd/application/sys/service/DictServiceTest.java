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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private MockedStatic<DictType> mockedDictType;
    private MockedStatic<DictData> mockedDictData;

    @BeforeEach
    void setUp() {
        mockedDictType = mockStatic(DictType.class);
        mockedDictData = mockStatic(DictData.class);
    }

    @AfterEach
    void tearDown() {
        mockedDictType.close();
        mockedDictData.close();
    }

    @Nested
    @DisplayName("DictType")
    class DictTypeTests {

        @Test
        void createType_Success() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setCode("test_code");
            dto.setName("Test Dict");
            dto.setSort(1);
            dto.setRemark("Remark");

            DictType mockEntity = mock(DictType.class);
            DictTypeDTO mockDto = new DictTypeDTO();

            when(dictTypeRepository.existsByCode("test_code")).thenReturn(false);
            mockedDictType.when(() -> DictType.create(dto.getName(), dto.getCode(), dto.getSort(), dto.getRemark())).thenReturn(mockEntity);
            when(dictTypeRepository.save(mockEntity)).thenReturn(mockEntity);
            when(dictTypeMapper.toDto(mockEntity)).thenReturn(mockDto);

            DictTypeDTO result = dictService.createType(dto);

            assertSame(mockDto, result);
            mockedDictType.verify(() -> DictType.create(dto.getName(), dto.getCode(), dto.getSort(), dto.getRemark()));
            verify(dictTypeRepository).save(mockEntity);
        }

        @Test
        void createType_DuplicateCode_ThrowsException() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setCode("existing_code");
            when(dictTypeRepository.existsByCode("existing_code")).thenReturn(true);
            assertThrows(BizException.class, () -> dictService.createType(dto));
        }

        @Test
        void updateType_Success() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setId(1L);
            dto.setName("New Name");
            dto.setSort(10);
            dto.setRemark("New Remark");

            DictType mockEntity = mock(DictType.class);
            when(mockEntity.getIsSystem()).thenReturn(false);
            when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(dictTypeRepository.save(mockEntity)).thenReturn(mockEntity);

            dictService.updateType(dto);

            verify(mockEntity).updateInfo(dto.getName(), dto.getSort(), dto.getRemark());
            verify(dictTypeRepository).save(mockEntity);
        }

        @Test
        void updateType_SystemModifyCode_ThrowsException() {
            DictTypeSaveDTO dto = new DictTypeSaveDTO();
            dto.setId(1L);
            dto.setCode("new_code");
            DictType mockEntity = mock(DictType.class);
            when(mockEntity.getIsSystem()).thenReturn(true);
            when(mockEntity.getCode()).thenReturn("sys_code");
            when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(mockEntity));

            assertThrows(BizException.class, () -> dictService.updateType(dto));
        }

        @Test
        void deleteType_Success() {
            Long id = 1L;
            DictType mockEntity = mock(DictType.class);
            when(mockEntity.getIsSystem()).thenReturn(false);
            when(mockEntity.getCode()).thenReturn("test_code");
            when(dictTypeRepository.findById(id)).thenReturn(Optional.of(mockEntity));

            dictService.deleteType(id);

            verify(dictDataRepository).deleteByTypeCode("test_code");
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "test_code");
            verify(dictTypeRepository).delete(mockEntity);
        }

        @Test
        void deleteType_System_ThrowsException() {
            Long id = 1L;
            DictType mockEntity = mock(DictType.class);
            when(mockEntity.getIsSystem()).thenReturn(true);
            when(dictTypeRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            assertThrows(BizException.class, () -> dictService.deleteType(id));
        }

        @Test
        void findPage_Success() {
            Page<DictType> page = new PageImpl<>(Collections.singletonList(mock(DictType.class)));
            when(dictTypeRepository.findAll(any(com.querydsl.core.types.Predicate.class), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);
            dictService.findPage(mock(com.querydsl.core.types.Predicate.class), mock(org.springframework.data.domain.Pageable.class));
            verify(dictTypeMapper, atLeastOnce()).toDto(any(DictType.class));
        }
    }

    @Nested
    @DisplayName("DictData")
    class DictDataTests {

        @Test
        void createData_Success() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setTypeCode("test_code");
            dto.setLabel("Label");
            dto.setValue("Value");
            dto.setSort(1);
            dto.setCssClass("css");
            dto.setListClass("list");
            dto.setIsDefault(true);
            dto.setRemark("remark");

            DictType mockType = mock(DictType.class);
            when(mockType.getIsSystem()).thenReturn(false);
            DictData mockEntity = mock(DictData.class);
            DictDataDTO mockDto = new DictDataDTO();

            when(dictTypeRepository.findByCode("test_code")).thenReturn(Optional.of(mockType));
            mockedDictData.when(() -> DictData.create(dto.getTypeCode(), dto.getLabel(), dto.getValue(), dto.getSort(), dto.getCssClass(), dto.getListClass(), dto.getIsDefault(), dto.getRemark())).thenReturn(mockEntity);
            when(dictDataRepository.save(mockEntity)).thenReturn(mockEntity);
            when(dictDataMapper.toDto(mockEntity)).thenReturn(mockDto);

            dictService.createData(dto);

            verify(dictDataRepository).save(mockEntity);
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "test_code");
        }

        @Test
        void createData_System_ThrowsException() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setTypeCode("sys_code");
            DictType mockType = mock(DictType.class);
            when(mockType.getIsSystem()).thenReturn(true);
            when(dictTypeRepository.findByCode("sys_code")).thenReturn(Optional.of(mockType));
            assertThrows(BizException.class, () -> dictService.createData(dto));
        }

        @Test
        void updateData_ChangeTypeCode_Success() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setId(1L);
            dto.setTypeCode("new_code");
            dto.setLabel("New Label");
            dto.setValue("new_value");
            dto.setSort(10);
            dto.setCssClass("new_css");
            dto.setListClass("new_list");
            dto.setIsDefault(false);
            dto.setRemark("new_remark");

            DictData mockEntity = mock(DictData.class);
            DictType mockType = mock(DictType.class);

            when(mockEntity.getTypeCode()).thenReturn("old_code", "old_code", "new_code");

            when(mockType.getIsSystem()).thenReturn(false);
            when(dictDataRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
            when(dictTypeRepository.findByCode("old_code")).thenReturn(Optional.of(mockType));
            when(dictDataRepository.save(mockEntity)).thenReturn(mockEntity);

            dictService.updateData(dto);

            verify(mockEntity).updateInfo(dto.getLabel(), dto.getValue(), dto.getSort(), dto.getCssClass(), dto.getListClass(), dto.getIsDefault(), dto.getRemark());
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "old_code");
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "new_code");
        }

        @Test
        void deleteData_Success() {
            Long id = 1L;
            DictData mockEntity = mock(DictData.class);
            DictType mockType = mock(DictType.class);
            when(mockEntity.getTypeCode()).thenReturn("test_code");
            when(mockType.getIsSystem()).thenReturn(false);
            when(dictDataRepository.findById(id)).thenReturn(Optional.of(mockEntity));
            when(dictTypeRepository.findByCode("test_code")).thenReturn(Optional.of(mockType));

            dictService.deleteData(id);

            verify(dictDataRepository).delete(mockEntity);
            verify(redisTemplate).delete(Const.CacheKey.DICT_DATA + "test_code");
        }
    }
}
