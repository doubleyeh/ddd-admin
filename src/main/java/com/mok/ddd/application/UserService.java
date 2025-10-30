package com.mok.ddd.application;

import com.mok.ddd.application.dto.UserDTO;
import com.mok.ddd.application.dto.UserPostDTO;
import com.mok.ddd.application.dto.UserPutDTO;
import com.mok.ddd.application.dto.UserQuery;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.UserMapper;
import com.mok.ddd.application.service.BaseServiceImpl;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.QUser;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
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

    @Transactional
    public UserDTO create(UserPostDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User entity = userMapper.postToEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDTO updateUser(UserPutDTO dto) {
        User entity = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        userMapper.putToEntity(dto, entity);
        return this.toDto(userRepository.save(entity));
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<User> userToDelete = userRepository.findById(id);

        if(userToDelete.isEmpty()){
            throw new NotFoundException();
        }
        if (SysUtil.isSuperAdmin(userToDelete.get().getTenantId(), userToDelete.get().getUsername())) {
            throw new BizException("用户不允许删除");
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
}
