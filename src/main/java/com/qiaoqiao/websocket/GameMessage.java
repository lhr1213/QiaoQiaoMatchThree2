// WebSocket消息对象
package com.qiaoqiao.websocket;

import com.qiaoqiao.model.game.Board;
import lombok.Data;

@Data
public class GameMessage {
    private String type;
    private String gameId;
    private String userId;
    private Board board;
    private int score;
    private int movesLeft;
    private int row1;
    private int col1;
    private int row2;
    private int col2;
    private String message;
    private boolean success;
    private boolean gameOver;

    // 构造函数
    public GameMessage() {
    }

    // 创建游戏消息
    public static GameMessage createGameMessage(String type, String gameId, String userId, Board board) {
        GameMessage message = new GameMessage();
        message.setType(type);
        message.setGameId(gameId);
        message.setUserId(userId);
        message.setBoard(board);
        if (board != null) {
            message.setScore(board.getScore());
            message.setMovesLeft(board.getMovesLeft());
        }
        return message;
    }

    // 创建移动消息
    public static GameMessage createMoveMessage(String gameId, String userId, int row1, int col1, int row2, int col2) {
        GameMessage message = new GameMessage();
        message.setType("move");
        message.setGameId(gameId);
        message.setUserId(userId);
        message.setRow1(row1);
        message.setCol1(col1);
        message.setRow2(row2);
        message.setCol2(col2);
        return message;
    }

    // 创建游戏结束消息
    public static GameMessage createGameOverMessage(String gameId, String userId, int score) {
        GameMessage message = new GameMessage();
        message.setType("gameOver");
        message.setGameId(gameId);
        message.setUserId(userId);
        message.setScore(score);
        message.setGameOver(true);
        return message;
    }

    // 创建错误消息
    public static GameMessage createErrorMessage(String gameId, String userId, String errorMessage) {
        GameMessage message = new GameMessage();
        message.setType("error");
        message.setGameId(gameId);
        message.setUserId(userId);
        message.setMessage(errorMessage);
        message.setSuccess(false);
        return message;
    }

    // 创建通知消息
    public static GameMessage createNotificationMessage(String gameId, String userId, String notificationMessage) {
        GameMessage message = new GameMessage();
        message.setType("notification");
        message.setGameId(gameId);
        message.setUserId(userId);
        message.setMessage(notificationMessage);
        message.setSuccess(true);
        return message;
    }
}