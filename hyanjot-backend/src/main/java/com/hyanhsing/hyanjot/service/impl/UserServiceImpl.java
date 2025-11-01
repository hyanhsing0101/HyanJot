package com.hyanhsing.hyanjot.service.impl;

import com.hyanhsing.hyanjot.entity.User;
import com.hyanhsing.hyanjot.repository.UserRepository;
import com.hyanhsing.hyanjot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String username, String password, String nickname) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 密码加密
        user.setNickname(nickname);
        user.setStatus(1); // 默认正常状态

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        Objects.requireNonNull(user, "User不能为空");
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        userRepository.deleteById(id);
    }
}