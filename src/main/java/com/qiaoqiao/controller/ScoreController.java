// 分数控制器
package com.qiaoqiao.controller;

import com.qiaoqiao.model.Score;
import com.qiaoqiao.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    // 排行榜页面
    @GetMapping("/leaderboard")
    public String leaderboardPage(Model model) {
        // 获取前50名最高分数
        List<Score> scores = scoreService.getLeaderboard(50);
        model.addAttribute("scores", scores);
        model.addAttribute("title", "全球排行榜");
        return "leaderboard";
    }

    // 按游戏模式查看排行榜
    @GetMapping("/leaderboard/{gameMode}")
    public String leaderboardByGameMode(@PathVariable String gameMode, Model model) {
        List<Score> scores = scoreService.getLeaderboardByGameMode(gameMode);
        model.addAttribute("scores", scores);
        model.addAttribute("title", gameMode + "模式排行榜");
        return "leaderboard";
    }

    // 用户游戏记录页面
    @GetMapping("/history")
    public String userScoresPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        List<Score> scores = scoreService.getUserScores(userId);
        model.addAttribute("scores", scores);

        // 获取用户平均分数
        Double avgScore = scoreService.getUserAverageScore(userId);
        model.addAttribute("averageScore", avgScore);

        // 获取用户游戏次数
        Long gamesCount = scoreService.getUserGamesCount(userId);
        model.addAttribute("gamesCount", gamesCount);

        return "score-history";
    }

    // 保存分数（API）
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveScore(@RequestBody Map<String, Object> scoreData,
                                       HttpSession session) {
        // 获取用户ID，如果没有则为游客
        Long userId = (Long) session.getAttribute("userId");

        // 从请求中获取分数
        int score;
        try {
            if (scoreData.get("score") instanceof Integer) {
                score = (Integer) scoreData.get("score");
            } else if (scoreData.get("score") instanceof String) {
                score = Integer.parseInt((String) scoreData.get("score"));
            } else {
                throw new NumberFormatException("无效的分数格式");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "无效的分数格式"
            ));
        }

        try {
            Score savedScore;

            if (userId != null) {
                // 已登录用户
                savedScore = scoreService.saveScore(userId, score);
            } else {
                // 游客
                savedScore = scoreService.saveGuestScore(score);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "score", savedScore
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 保存详细分数（API）
    @PostMapping("/api/save-detailed")
    @ResponseBody
    public ResponseEntity<?> saveDetailedScore(@RequestBody Map<String, Object> scoreData,
                                               HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "请先登录"
            ));
        }

        try {
            // 从请求中获取数据并转换为正确的类型
            int score;
            int boardSize;
            int movesUsed;
            boolean completed;

            try {
                score = getIntValue(scoreData.get("score"));
                boardSize = getIntValue(scoreData.get("boardSize"));
                movesUsed = getIntValue(scoreData.get("movesUsed"));

                if (scoreData.get("completed") instanceof Boolean) {
                    completed = (Boolean) scoreData.get("completed");
                } else if (scoreData.get("completed") instanceof String) {
                    completed = Boolean.parseBoolean((String) scoreData.get("completed"));
                } else {
                    throw new IllegalArgumentException("无效的completed字段格式");
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "无效的数据格式: " + e.getMessage()
                ));
            }

            Score savedScore = scoreService.saveDetailedScore(
                    userId, score, boardSize, movesUsed, completed
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "score", savedScore
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 获取用户最高分数（API）
    @GetMapping("/api/top/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserTopScores(@PathVariable String userId,
                                              @RequestParam(defaultValue = "10") int limit) {
        try {
            Long userIdLong = Long.parseLong(userId);
            List<Score> topScores = scoreService.getUserTopScores(userIdLong, limit);
            return ResponseEntity.ok(topScores);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的用户ID格式"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 获取排行榜（API）
    @GetMapping("/api/leaderboard")
    @ResponseBody
    public ResponseEntity<?> getLeaderboard(@RequestParam(defaultValue = "20") int limit) {
        List<Score> leaderboard = scoreService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    // 获取特定游戏模式的排行榜（API）
    @GetMapping("/api/leaderboard/{gameMode}")
    @ResponseBody
    public ResponseEntity<?> getLeaderboardByGameMode(@PathVariable String gameMode) {
        List<Score> leaderboard = scoreService.getLeaderboardByGameMode(gameMode);
        return ResponseEntity.ok(leaderboard);
    }

    // 获取最近分数记录（API）
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<?> getRecentScores(@RequestParam(defaultValue = "7") int days) {
        List<Score> recentScores = scoreService.getRecentScores(days);
        return ResponseEntity.ok(recentScores);
    }

    // 帮助方法：获取整数值
    private int getIntValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else {
            throw new NumberFormatException("无法转换为整数: " + value);
        }
    }
}