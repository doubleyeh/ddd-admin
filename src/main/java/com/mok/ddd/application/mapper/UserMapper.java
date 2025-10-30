package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.UserDTO;
import com.mok.ddd.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    //继承字段必须明确指出
    @Mapping(source = "id", target = "id")
    UserDTO toDto(User entity);

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