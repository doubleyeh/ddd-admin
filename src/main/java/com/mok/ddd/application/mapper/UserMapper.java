package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.dto.user.UserPutDTO;
import com.mok.ddd.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { RoleMapper.class })
public interface UserMapper {
    // 继承字段必须明确指出
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "roles", source = "roles")
    UserDTO toDto(User entity);

    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    User postToEntity(UserPostDTO dto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void putToEntity(UserPutDTO dto, @MappingTarget User entity);

    default Page<UserDTO> toDtoPage(Page<User> entityPage) {
        if (entityPage == null) {
            return Page.empty();
        }

        List<UserDTO> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        Pageable pageable = entityPage.getPageable();
        long total = entityPage.getTotalElements();

        return new PageImpl<>(dtoList, pageable, total);
    }
}