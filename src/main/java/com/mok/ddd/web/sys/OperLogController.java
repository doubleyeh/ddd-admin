package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.log.OperLogDTO;
import com.mok.ddd.application.sys.dto.log.OperLogQuery;
import com.mok.ddd.application.sys.service.OperLogService;
import com.mok.ddd.web.common.RestResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oper-logs")
@RequiredArgsConstructor
public class OperLogController {

    private final OperLogService operLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('log:oper:list')")
    public RestResponse<Page<@NonNull OperLogDTO>> findPage(OperLogQuery query, Pageable pageable) {
        Page<@NonNull OperLogDTO> page = operLogService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }
}
