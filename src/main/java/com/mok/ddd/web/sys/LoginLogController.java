package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.log.LoginLogDTO;
import com.mok.ddd.application.sys.dto.log.LoginLogQuery;
import com.mok.ddd.application.sys.service.LoginLogService;
import com.mok.ddd.web.common.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('log:login:list')")
    public RestResponse<Page<LoginLogDTO>> findPage(LoginLogQuery query, Pageable pageable) {
        Page<LoginLogDTO> page = loginLogService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }
}
