package com.hyanhsing.hyanjot.service;

import com.hyanhsing.hyanjot.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    // 用户注册
    User register(String username, String password, String nickname);

    // 根据用户名查找
    Optional<User> findByUsername(String username);

    // 根据ID查找
    Optional<User> findById(Long id);

    // 获取所有用户
    List<User> findAll();

    // 更新用户信息
    User updateUser(User user);

    // 删除用户
    void deleteUser(Long id);
}