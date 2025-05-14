// 游戏服务
package com.qiaoqiao.service;

import com.qiaoqiao.model.game.Board;
import com.qiaoqiao.model.game.GameSession;
import com.qiaoqiao.model.game.GameState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    // 存储游戏会话
    private final Map<String, GameSession> gameSessionMap = new ConcurrentHashMap<>();

    // 创建新游戏
    public GameSession createNewGame() {
        // 创建8x8大小的游戏面板，初始移动次数为20
        Board board = new Board(8, 8, 20);
        String gameId = UUID.randomUUID().toString();
        GameSession gameSession = new GameSession(gameId, null, board);
        gameSession.setState(GameState.READY);

        // 保存到会话Map
        gameSessionMap.put(gameId, gameSession);

        return gameSession;
    }

    // 为特定用户创建新游戏
    public GameSession createNewGameForUser(String userId) {
        Board board = new Board(8, 8, 20);
        String gameId = UUID.randomUUID().toString();
        GameSession gameSession = new GameSession(gameId, userId, board);
        gameSession.setState(GameState.READY);

        // 保存到会话Map
        gameSessionMap.put(gameId, gameSession);

        return gameSession;
    }

    // 获取游戏会话
    public GameSession getGameSessionById(String gameId) {
        return gameSessionMap.get(gameId);
    }

    // 执行移动操作
    public boolean makeMove(GameSession gameSession, int row1, int col1, int row2, int col2) {
        // 检查游戏状态
        if (gameSession.getState() != GameState.PLAYING && gameSession.getState() != GameState.READY) {
            return false;
        }

        // 如果是READY状态，切换到PLAYING
        if (gameSession.getState() == GameState.READY) {
            gameSession.setState(GameState.PLAYING);
        }

        Board board = gameSession.getBoard();
        boolean moveSuccess = board.swapTiles(row1, col1, row2, col2);

        // 检查游戏是否结束
        if (board.isGameOver()) {
            gameSession.setState(GameState.GAME_OVER);
        }

        return moveSuccess;
    }

    // 检查特定位置是否有可能的移动
    public boolean hasPossibleMoves(GameSession gameSession) {
        Board board = gameSession.getBoard();
        int rows = board.getRows();
        int columns = board.getColumns();

        // 检查每个位置的四个方向
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // 向右交换
                if (j < columns - 1) {
                    if (checkPotentialMatch(board, i, j, i, j + 1)) {
                        return true;
                    }
                }

                // 向下交换
                if (i < rows - 1) {
                    if (checkPotentialMatch(board, i, j, i + 1, j)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 检查交换两个位置是否能形成匹配
    private boolean checkPotentialMatch(Board board, int row1, int col1, int row2, int col2) {
        // 获取瓦片类型
        String type1 = board.getTiles()[row1][col1].getType();
        String type2 = board.getTiles()[row2][col2].getType();

        // 临时交换瓦片
        board.getTiles()[row1][col1].setType(type2);
        board.getTiles()[row2][col2].setType(type1);

        boolean hasMatch = board.hasMatches();

        // 交换回来
        board.getTiles()[row1][col1].setType(type1);
        board.getTiles()[row2][col2].setType(type2);

        return hasMatch;
    }
}