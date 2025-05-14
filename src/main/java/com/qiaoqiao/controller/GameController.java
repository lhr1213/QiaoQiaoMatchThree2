// 游戏控制器
package com.qiaoqiao.controller;

import com.qiaoqiao.model.game.Board;
import com.qiaoqiao.model.game.GameSession;
import com.qiaoqiao.service.GameService;
import com.qiaoqiao.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private ScoreService scoreService;

    // 游戏主页
    @GetMapping
    public String gamePage(Model model, HttpSession session) {
        // 获取或创建游戏会话
        GameSession gameSession = (GameSession) session.getAttribute("gameSession");
        if (gameSession == null) {
            gameSession = gameService.createNewGame();
            session.setAttribute("gameSession", gameSession);
        }

        model.addAttribute("gameSession", gameSession);
        return "game";
    }

    // 开始新游戏
    @GetMapping("/new")
    public String newGame(HttpSession session) {
        GameSession gameSession = gameService.createNewGame();
        session.setAttribute("gameSession", gameSession);
        return "redirect:/game";
    }

    // 获取游戏状态
    @GetMapping("/state")
    @ResponseBody
    public ResponseEntity<Board> getGameState(HttpSession session) {
        GameSession gameSession = (GameSession) session.getAttribute("gameSession");
        if (gameSession == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(gameSession.getBoard());
    }

    // 移动瓦片
    @PostMapping("/move")
    @ResponseBody
    public ResponseEntity<?> makeMove(@RequestBody Map<String, Integer> moveData, HttpSession session) {
        GameSession gameSession = (GameSession) session.getAttribute("gameSession");
        if (gameSession == null) {
            return ResponseEntity.badRequest().build();
        }

        int row1 = moveData.get("row1");
        int col1 = moveData.get("col1");
        int row2 = moveData.get("row2");
        int col2 = moveData.get("col2");

        boolean moveSuccess = gameService.makeMove(gameSession, row1, col1, row2, col2);

        if (moveSuccess) {
            // 检查游戏是否结束
            if (gameSession.getBoard().isGameOver()) {
                // 获取userId，确保是Long类型
                Long userId = null;
                if (gameSession.getUserId() != null) {
                    try {
                        userId = Long.parseLong(gameSession.getUserId());
                    } catch (NumberFormatException e) {
                        // 处理userId不是数字的情况
                        // 如果userId不是数字，则当作游客处理
                        scoreService.saveGuestScore(gameSession.getBoard().getScore());
                    }
                }

                // 保存分数
                if (userId != null) {
                    scoreService.saveScore(userId, gameSession.getBoard().getScore());
                }

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "gameOver", true,
                        "score", gameSession.getBoard().getScore()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "gameOver", false,
                    "board", gameSession.getBoard()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "无效的移动"
            ));
        }
    }
}