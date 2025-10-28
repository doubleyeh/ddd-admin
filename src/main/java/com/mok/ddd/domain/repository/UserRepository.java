package com.mok.ddd.domain.repository;

import com.mok.ddd.domain.entity.User;
import com.mok.ddd.infrastructure.repository.CustomRepository;

import java.util.Optional;

public interface UserRepository extends CustomRepository<User, String> {
    Optional<User> findByUsername(String username);
}