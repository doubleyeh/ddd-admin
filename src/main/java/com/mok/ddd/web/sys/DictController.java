package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.dict.*;
import com.mok.ddd.application.sys.service.DictService;
import com.mok.ddd.web.common.RestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/type")
    @PreAuthorize("hasAuthority('dict:list')")
    public RestResponse<Page<DictTypeDTO>> findTypePage(DictTypeQuery query, Pageable pageable) {
        return RestResponse.success(dictService.findPage(query.toPredicate(), pageable));
    }

    @PostMapping("/type")
    @PreAuthorize("hasAuthority('dict:create')")
    public RestResponse<DictTypeDTO> createType(@RequestBody @Valid DictTypeSaveDTO dto) {
        return RestResponse.success(dictService.createType(dto));
    }

    @PutMapping("/type")
    @PreAuthorize("hasAuthority('dict:update')")
    public RestResponse<DictTypeDTO> updateType(@RequestBody @Valid DictTypeSaveDTO dto) {
        return RestResponse.success(dictService.updateType(dto));
    }

    @DeleteMapping("/type/{id}")
    @PreAuthorize("hasAuthority('dict:delete')")
    public RestResponse<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return RestResponse.success();
    }

    @GetMapping("/data/{typeCode}")
    public RestResponse<List<DictDataDTO>> getDataByType(@PathVariable String typeCode) {
        return RestResponse.success(dictService.getDataByType(typeCode));
    }

    @PostMapping("/data")
    @PreAuthorize("hasAuthority('dict:create')")
    public RestResponse<DictDataDTO> createData(@RequestBody @Valid DictDataSaveDTO dto) {
        return RestResponse.success(dictService.createData(dto));
    }

    @PutMapping("/data")
    @PreAuthorize("hasAuthority('dict:update')")
    public RestResponse<DictDataDTO> updateData(@RequestBody @Valid DictDataSaveDTO dto) {
        return RestResponse.success(dictService.updateData(dto));
    }

    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAuthority('dict:delete')")
    public RestResponse<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return RestResponse.success();
    }
}
