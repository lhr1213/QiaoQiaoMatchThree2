// 用户服务
package com.qiaoqiao.service;

import com.qiaoqiao.model.User;
import com.qiaoqiao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 注册新用户
    public User registerUser(String username, String password, String email) {
        // 检查用户名和邮箱是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNickname(username);
        user.setHighestScore(0);
        user.setTotalGames(0);

        return userRepository.save(user);
    }

    // 用户登录
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (passwordEncoder.matches(password, user.getPassword())) {
                // 更新最后登录时间
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return user;
            }
        }

        throw new RuntimeException("用户名或密码错误");
    }

    // 更新用户信息
    public User updateUserProfile(Long userId, String nickname, String avatar) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (nickname != null && !nickname.isEmpty()) {
                user.setNickname(nickname);
            }

            if (avatar != null && !avatar.isEmpty()) {
                user.setAvatar(avatar);
            }

            return userRepository.save(user);
        }

        throw new RuntimeException("用户不存在");
    }

    // 修改密码
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return;
            }

            throw new RuntimeException("原密码不正确");
        }

        throw new RuntimeException("用户不存在");
    }

    // 获取用户信息
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 获取用户信息（通过用户名）
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 更新用户最高分
    public void updateHighestScore(Long userId, int score) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.updateHighestScore(score);
            userRepository.save(user);
        }
    }

    // 增加用户游戏次数
    public void incrementGamesCount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementGamesCount();
            userRepository.save(user);
        }
    }

    // 获取排行榜用户
    public List<User> getLeaderboardUsers(int limit) {
        return userRepository.findTopNUsersByHighestScore(limit);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 检查用户名是否存在
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // 检查邮箱是否存在
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}