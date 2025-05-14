// WebSocket处理器
package com.qiaoqiao.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaoqiao.model.game.Board;
import com.qiaoqiao.model.game.GameSession;
import com.qiaoqiao.service.GameService;
import com.qiaoqiao.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private GameService gameService;

    @Autowired
    private ScoreService scoreService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存储游戏会话和WebSocket会话的对应关系
    private final Map<String, WebSocketSession> gameSessionMap = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionGameMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取HTTP会话中的游戏会话ID
        String gameId = (String) session.getAttributes().get("gameId");

        if (gameId != null) {
            // 将WebSocket会话与游戏会话关联
            gameSessionMap.put(gameId, session);
            sessionGameMap.put(session, gameId);

            // 发送游戏状态
            sendGameState(gameId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 解析消息
            GameMessage gameMessage = objectMapper.readValue(message.getPayload(), GameMessage.class);
            String gameId = gameMessage.getGameId();
            String userId = gameMessage.getUserId();

            // 处理不同类型的消息
            switch (gameMessage.getType()) {
                case "join":
                    handleJoinGame(session, gameId, userId);
                    break;
                case "move":
                    handleMove(session, gameId, userId, gameMessage);
                    break;
                case "newGame":
                    handleNewGame(session, gameId, userId);
                    break;
                default:
                    sendErrorMessage(session, gameId, userId, "未知的消息类型");
            }
        } catch (Exception e) {
            // 发送错误消息
            sendErrorMessage(session, null, null, "消息处理错误: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 清理会话关联
        String gameId = sessionGameMap.remove(session);
        if (gameId != null) {
            gameSessionMap.remove(gameId);
        }
    }

    // 处理加入游戏
    private void handleJoinGame(WebSocketSession session, String gameId, String userId) throws IOException {
        if (gameId == null) {
            // 创建新游戏
            GameSession gameSession = gameService.createNewGame();
            gameId = gameSession.getId();
        }

        // 将WebSocket会话与游戏会话关联
        gameSessionMap.put(gameId, session);
        sessionGameMap.put(session, gameId);

        // 发送游戏状态
        sendGameState(gameId, session);
    }

    // 处理移动操作
    private void handleMove(WebSocketSession session, String gameId, String userId, GameMessage moveMessage) throws IOException {
        GameSession gameSession = gameService.getGameSessionById(gameId);

        if (gameSession == null) {
            sendErrorMessage(session, gameId, userId, "游戏会话不存在");
            return;
        }

        int row1 = moveMessage.getRow1();
        int col1 = moveMessage.getCol1();
        int row2 = moveMessage.getRow2();
        int col2 = moveMessage.getCol2();

        // 执行移动
        boolean moveSuccess = gameService.makeMove(gameSession, row1, col1, row2, col2);

        if (moveSuccess) {
            // 检查游戏是否结束
            if (gameSession.getBoard().isGameOver()) {
                // 处理游戏结束
                if (userId != null) {
                    // 尝试将userId转换为Long
                    try {
                        Long userIdLong = Long.parseLong(userId);
                        scoreService.saveScore(userIdLong, gameSession.getBoard().getScore());
                    } catch (NumberFormatException e) {
                        // userId不是数字，保存为游客分数
                        scoreService.saveGuestScore(gameSession.getBoard().getScore());
                    }
                } else {
                    // 用户ID为空，保存为游客分数
                    scoreService.saveGuestScore(gameSession.getBoard().getScore());
                }

                // 发送游戏结束消息
                sendGameOverMessage(session, gameId, userId, gameSession.getBoard().getScore());
            } else {
                // 发送更新的游戏状态
                sendGameState(gameId, session);
            }
        } else {
            // 发送无效移动消息
            sendErrorMessage(session, gameId, userId, "无效的移动");
        }
    }

    // 处理新游戏请求
    private void handleNewGame(WebSocketSession session, String gameId, String userId) throws IOException {
        // 创建新游戏
        GameSession gameSession;

        if (userId != null) {
            // 为用户创建新游戏
            gameSession = gameService.createNewGameForUser(userId);
        } else {
            // 创建游客游戏
            gameSession = gameService.createNewGame();
        }

        // 更新会话关联
        String newGameId = gameSession.getId();
        gameSessionMap.put(newGameId, session);
        sessionGameMap.put(session, newGameId);

        // 发送游戏状态
        sendGameState(newGameId, session);
    }

    // 发送游戏状态
    private void sendGameState(String gameId, WebSocketSession session) throws IOException {
        GameSession gameSession = gameService.getGameSessionById(gameId);

        if (gameSession != null) {
            GameMessage gameStateMessage = GameMessage.createGameMessage(
                    "gameState", gameId, gameSession.getUserId(), gameSession.getBoard()
            );

            sendMessage(session, gameStateMessage);
        }
    }

    // 发送游戏结束消息
    private void sendGameOverMessage(WebSocketSession session, String gameId, String userId, int score) throws IOException {
        GameMessage gameOverMessage = GameMessage.createGameOverMessage(gameId, userId, score);
        sendMessage(session, gameOverMessage);
    }

    // 发送错误消息
    private void sendErrorMessage(WebSocketSession session, String gameId, String userId, String errorMessage) throws IOException {
        GameMessage errorMsg = GameMessage.createErrorMessage(gameId, userId, errorMessage);
        sendMessage(session, errorMsg);
    }

    // 发送消息
    private void sendMessage(WebSocketSession session, GameMessage message) throws IOException {
        if (session != null && session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    // 广播消息到所有会话
    public void broadcastMessage(GameMessage message) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(message);

        for (WebSocketSession session : gameSessionMap.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }
}