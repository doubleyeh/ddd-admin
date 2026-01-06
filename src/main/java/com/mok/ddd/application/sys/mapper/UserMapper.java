package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.user.UserDTO;
import com.mok.ddd.domain.sys.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { RoleMapper.class })
public interface UserMapper {
    // 继承字段必须明确指出
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "roles", source = "roles")
    UserDTO toDto(User entity);
}
