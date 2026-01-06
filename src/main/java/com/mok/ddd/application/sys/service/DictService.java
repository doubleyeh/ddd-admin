package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
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
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.util.QuerydslUtils;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictService extends BaseServiceImpl<DictType, Long, DictTypeDTO> {

    private final DictTypeRepository dictTypeRepository;
    private final DictDataRepository dictDataRepository;
    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected CustomRepository<DictType, Long> getRepository() {
        return dictTypeRepository;
    }

    @Override
    protected DictType toEntity(@NonNull DictTypeDTO dto) {
        throw new UnsupportedOperationException("不支持从DTO创建或更新实体。");
    }

    @Override
    protected DictTypeDTO toDto(@NonNull DictType entity) {
        return dictTypeMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull DictTypeDTO> findPage(Predicate predicate, Pageable pageable) {
        Pageable qSortPageable = QuerydslUtils.convertToQSortPageable(pageable, DictType.class, "dictType");
        return super.findPage(predicate, qSortPageable);
    }

    @Transactional
    public DictTypeDTO createType(DictTypeSaveDTO dto) {
        if (dictTypeRepository.existsByCode(dto.getCode())) {
            throw new BizException("字典类型编码已存在");
        }
        DictType entity = DictType.create(dto.getName(), dto.getCode(), dto.getSort(), dto.getRemark());
        return dictTypeMapper.toDto(dictTypeRepository.save(entity));
    }

    @Transactional
    public DictTypeDTO updateType(DictTypeSaveDTO dto) {
        DictType entity = dictTypeRepository.findById(dto.getId())
                .orElseThrow(NotFoundException::new);

        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            if (!entity.getCode().equals(dto.getCode())) {
                throw new BizException("系统内置字典禁止修改编码");
            }
        }

        entity.updateInfo(dto.getName(), dto.getSort(), dto.getRemark());
        return dictTypeMapper.toDto(dictTypeRepository.save(entity));
    }

    @Transactional
    public void deleteType(Long id) {
        DictType entity = dictTypeRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new BizException("系统内置字典禁止删除");
        }

        dictDataRepository.deleteByTypeCode(entity.getCode());
        redisTemplate.delete(Const.CacheKey.DICT_DATA + entity.getCode());

        dictTypeRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<DictDataDTO> getDataByType(String typeCode) {
        return dictDataRepository.findByTypeCodeOrderBySortAsc(typeCode).stream()
                .map(dictDataMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DictDataDTO createData(DictDataSaveDTO dto) {
        checkSystemDict(dto.getTypeCode());
        DictData entity = DictData.create(dto.getTypeCode(), dto.getLabel(), dto.getValue(), dto.getSort(), dto.getCssClass(), dto.getListClass(), dto.getIsDefault(), dto.getRemark());
        DictData saved = dictDataRepository.save(entity);
        redisTemplate.delete(Const.CacheKey.DICT_DATA + dto.getTypeCode());
        return dictDataMapper.toDto(saved);
    }

    @Transactional
    public DictDataDTO updateData(DictDataSaveDTO dto) {
        DictData entity = dictDataRepository.findById(dto.getId())
                .orElseThrow(NotFoundException::new);
        checkSystemDict(entity.getTypeCode());

        String oldTypeCode = entity.getTypeCode();
        entity.updateInfo(dto.getLabel(), dto.getValue(), dto.getSort(), dto.getCssClass(), dto.getListClass(), dto.getIsDefault(), dto.getRemark());
        DictData saved = dictDataRepository.save(entity);

        redisTemplate.delete(Const.CacheKey.DICT_DATA + oldTypeCode);
        if (!oldTypeCode.equals(saved.getTypeCode())) {
            redisTemplate.delete(Const.CacheKey.DICT_DATA + saved.getTypeCode());
        }

        return dictDataMapper.toDto(saved);
    }

    @Transactional
    public void deleteData(Long id) {
        DictData entity = dictDataRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        checkSystemDict(entity.getTypeCode());
        dictDataRepository.delete(entity);
        redisTemplate.delete(Const.CacheKey.DICT_DATA + entity.getTypeCode());
    }

    private void checkSystemDict(String typeCode) {
        dictTypeRepository.findByCode(typeCode).ifPresent(type -> {
            if (Boolean.TRUE.equals(type.getIsSystem())) {
                throw new BizException("系统内置字典禁止修改数据");
            }
        });
    }
}
