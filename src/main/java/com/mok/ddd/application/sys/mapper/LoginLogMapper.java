package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.log.LoginLogDTO;
import com.mok.ddd.domain.sys.model.LoginLog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoginLogMapper {
    LoginLogDTO toDto(LoginLog loginLog);
}
