package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.dto.user.UserPutDTO;
import com.mok.ddd.domain.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { RoleMapper.class })
public interface UserMapper {
    // 继承字段必须明确指出
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "roles", source = "roles")
    UserDTO toDto(User entity);

    @Mapping(target = "roles", ignore = true)
    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User postToEntity(UserPostDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void putToEntity(UserPutDTO dto, @MappingTarget User entity);
}