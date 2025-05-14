// 用户服务测试类
package com.qiaoqiao.service;

import com.qiaoqiao.model.User;
import com.qiaoqiao.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    public void setup() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setNickname("Test User");
        testUser.setHighestScore(1000);
        testUser.setTotalGames(10);

        // 配置模拟行为
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(passwordEncoder.matches(eq("correctPassword"), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq("wrongPassword"), anyString())).thenReturn(false);
    }

    @Test
    public void testRegisterUser() {
        // 模拟用户名和邮箱检查
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // 模拟保存用户
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // 注册新用户
        User newUser = userService.registerUser("newuser", "password", "new@example.com");

        // 验证结果
        assertNotNull(newUser);
        assertEquals(2L, newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("new@example.com", newUser.getEmail());
        assertEquals("encodedPassword", newUser.getPassword());
        assertEquals("newuser", newUser.getNickname());
        assertEquals(0, newUser.getHighestScore());
        assertEquals(0, newUser.getTotalGames());

        // 验证方法调用
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingUsername() {
        // 模拟用户名已存在
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // 尝试注册已存在的用户名
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser("existinguser", "password", "new@example.com");
        });

        // 验证异常消息
        assertEquals("用户名已存在", exception.getMessage());

        // 验证方法调用
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLogin() {
        // 模拟查找用户
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // 尝试登录
        User loggedInUser = userService.login("testuser", "correctPassword");

        // 验证结果
        assertNotNull(loggedInUser);
        assertEquals(testUser.getId(), loggedInUser.getId());
        assertEquals(testUser.getUsername(), loggedInUser.getUsername());

        // 验证方法调用
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("correctPassword", "encodedPassword");
        verify(userRepository).save(testUser); // 最后登录时间更新
    }

    @Test
    public void testLoginWithWrongPassword() {
        // 模拟查找用户
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // 尝试使用错误密码登录
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login("testuser", "wrongPassword");
        });

        // 验证异常消息
        assertEquals("用户名或密码错误", exception.getMessage());

        // 验证方法调用
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUserProfile() {
        // 模拟查找用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 模拟保存用户
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 更新用户资料
        User updatedUser = userService.updateUserProfile(1L, "New Nickname", "new-avatar.jpg");

        // 验证结果
        assertNotNull(updatedUser);
        assertEquals("New Nickname", updatedUser.getNickname());
        assertEquals("new-avatar.jpg", updatedUser.getAvatar());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    public void testChangePassword() {
        // 模拟查找用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 模拟保存用户
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 修改密码
        userService.changePassword(1L, "correctPassword", "newPassword");

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("correctPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    public void testChangePasswordWithWrongOldPassword() {
        // 模拟查找用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 尝试使用错误的旧密码修改密码
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(1L, "wrongPassword", "newPassword");
        });

        // 验证异常消息
        assertEquals("原密码不正确", exception.getMessage());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateHighestScore() {
        // 模拟查找用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 模拟保存用户
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 更新最高分
        userService.updateHighestScore(1L, 2000);

        // 验证结果
        assertEquals(2000, testUser.getHighestScore());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    public void testUpdateHighestScoreLowerThanCurrent() {
        // 模拟查找用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 模拟保存用户
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 更新较低的分数
        userService.updateHighestScore(1L, 500);

        // 验证结果（分数应该保持不变）
        assertEquals(1000, testUser.getHighestScore());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    public void testIncrementGamesCount() {
        // 模拟查找用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 模拟保存用户
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 增加游戏次数
        userService.incrementGamesCount(1L);

        // 验证结果
        assertEquals(11, testUser.getTotalGames());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    public void testGetLeaderboardUsers() {
        // 模拟排行榜用户
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setHighestScore(3000);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setHighestScore(2000);

        List<User> leaderboardUsers = Arrays.asList(user1, user2);

        // 模拟查询结果
        when(userRepository.findTopNUsersByHighestScore(5)).thenReturn(leaderboardUsers);

        // 获取排行榜
        List<User> result = userService.getLeaderboardUsers(5);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());

        // 验证方法调用
        verify(userRepository).findTopNUsersByHighestScore(5);
    }
}