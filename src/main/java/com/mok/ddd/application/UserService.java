package com.mok.ddd.application;

import com.mok.ddd.application.dto.UserDTO;
import com.mok.ddd.application.dto.UserQuery;
import com.mok.ddd.application.mapper.UserMapper;
import com.mok.ddd.application.service.BaseServiceImpl;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.QUser;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService extends BaseServiceImpl<User, Long, UserDTO, UserQuery> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final QUser user = QUser.user;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO save(UserDTO dto) {
        Optional<User> existingUser = userRepository.findByUsername(dto.getUsername());

        if (existingUser.isPresent() && !existingUser.get().getId().equals(dto.getId())) {
            throw new RuntimeException("用户名已存在");
        }

        return super.save(dto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<User> userToDelete = userRepository.findById(id);

        if (SysUtil.isSuperAdmin(userToDelete.get().getTenantId(), userToDelete.get().getUsername())) {
            throw new RuntimeException("用户不允许删除");
        }

        super.deleteById(id);
    }

    @Override
    protected CustomRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected User toEntity(UserDTO dto) {
        return userMapper.toEntity(dto);
    }

    @Override
    protected UserDTO toDto(User entity) {
        return userMapper.toDto(entity);
    }

    @Override
    protected Predicate buildCondition(UserQuery query) {
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

        return builder.getValue();
    }

}
