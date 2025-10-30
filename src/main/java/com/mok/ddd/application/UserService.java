package com.mok.ddd.application;

import com.mok.ddd.application.dto.UserDTO;
import com.mok.ddd.application.dto.UserQuery;
import com.mok.ddd.application.mapper.UserMapper;
import com.mok.ddd.domain.entity.QUser;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final QUser user = QUser.user;


    @Transactional(readOnly = true)
    public Page<UserDTO> findPage(UserQuery query, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            builder.and(user.username.containsIgnoreCase(query.getUsername()));
        }

        if (query.getNickname() != null && !query.getNickname().isEmpty()) {
            builder.and(user.nickname.containsIgnoreCase(query.getNickname()));
        }

        if (query.getState() != null) {
            builder.and(user.state.eq(query.getState()));
        }

        Page<User> userPage = userRepository.findAll(builder, pageable);

        return userMapper.toDtoPage(userPage);
    }
}
