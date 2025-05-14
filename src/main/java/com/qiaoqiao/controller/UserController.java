// 用户控制器
package com.qiaoqiao.controller;

import com.qiaoqiao.model.User;
import com.qiaoqiao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 注册页面
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 注册处理
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           Model model) {
        try {
            User user = userService.registerUser(username, password, email);
            model.addAttribute("message", "注册成功，请登录");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // 登录页面
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 登录处理
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            return "redirect:/game";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // 登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 个人资料页面
    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/user/login";
        }
    }

    // 更新个人资料
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam(required = false) String avatar,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {
            User user = userService.updateUserProfile(userId, nickname, avatar);
            model.addAttribute("user", user);
            model.addAttribute("message", "个人资料更新成功");
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }

    // 修改密码
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session,
                                 Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {
            userService.changePassword(userId, oldPassword, newPassword);
            model.addAttribute("message", "密码修改成功");
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }

    // 检查用户名是否存在（API）
    @GetMapping("/api/check-username")
    @ResponseBody
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.isUsernameExists(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // 检查邮箱是否存在（API）
    @GetMapping("/api/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // 获取当前用户信息（API）
    @GetMapping("/api/current")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}