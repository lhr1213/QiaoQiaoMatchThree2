// 游戏会话模型
package com.qiaoqiao.model.game;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
public class GameSession {
    private String id;        // 游戏会话ID
    private String userId;    // 用户ID（可为null表示游客）
    private Board board;      // 游戏面板
    private GameState state;  // 游戏状态

    public GameSession(String id, String userId, Board board) {
        this.id = id;
        this.userId = userId;
        this.board = board;
        this.state = GameState.READY;
    }
}