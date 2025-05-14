// 游戏服务测试类
package com.qiaoqiao.service;

import com.qiaoqiao.model.game.Board;
import com.qiaoqiao.model.game.GameSession;
import com.qiaoqiao.model.game.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    private GameSession gameSession;

    @BeforeEach
    public void setup() {
        // 创建新游戏会话用于测试
        gameSession = gameService.createNewGame();
    }

    @Test
    public void testCreateNewGame() {
        // 验证创建新游戏
        GameSession newGame = gameService.createNewGame();

        assertNotNull(newGame, "游戏会话不应为空");
        assertNotNull(newGame.getId(), "游戏ID不应为空");
        assertNotNull(newGame.getBoard(), "游戏面板不应为空");
        assertEquals(GameState.READY, newGame.getState(), "新游戏应该处于READY状态");
        assertEquals(8, newGame.getBoard().getRows(), "默认行数应为8");
        assertEquals(8, newGame.getBoard().getColumns(), "默认列数应为8");
        assertEquals(20, newGame.getBoard().getMovesLeft(), "默认移动次数应为20");
        assertEquals(0, newGame.getBoard().getScore(), "初始分数应为0");
    }

    @Test
    public void testCreateNewGameForUser() {
        // 测试为特定用户创建游戏
        String userId = "test-user-123";
        GameSession userGame = gameService.createNewGameForUser(userId);

        assertNotNull(userGame, "用户游戏会话不应为空");
        assertEquals(userId, userGame.getUserId(), "用户ID应匹配");
    }

    @Test
    public void testPerformValidMove() {
        // 设置已知可以匹配的瓦片
        Board board = gameSession.getBoard();

        // 找到可能的移动
        int row1 = -1, col1 = -1, row2 = -1, col2 = -1;
        boolean foundMove = false;

        // 在测试中不能访问私有方法，所以手动查找可能的移动
        for (int i = 0; i < board.getRows() && !foundMove; i++) {
            for (int j = 0; j < board.getColumns() - 1 && !foundMove; j++) {
                // 临时交换相邻的瓦片看是否形成匹配
                String type1 = board.getTiles()[i][j].getType();
                String type2 = board.getTiles()[i][j+1].getType();

                board.getTiles()[i][j].setType(type2);
                board.getTiles()[i][j+1].setType(type1);

                if (hasPossibleMatch(board)) {
                    row1 = i;
                    col1 = j;
                    row2 = i;
                    col2 = j + 1;
                    foundMove = true;
                }

                // 恢复原状
                board.getTiles()[i][j].setType(type1);
                board.getTiles()[i][j+1].setType(type2);
            }
        }

        if (foundMove) {
            // 执行找到的有效移动
            gameSession.setState(GameState.PLAYING); // 设置为游戏进行中状态
            int initialScore = board.getScore();
            int initialMoves = board.getMovesLeft();

            boolean moveResult = gameService.makeMove(gameSession, row1, col1, row2, col2);

            assertTrue(moveResult, "有效匹配的移动应返回true");
            assertTrue(board.getScore() > initialScore, "分数应增加");
            assertEquals(initialMoves - 1, board.getMovesLeft(), "移动次数应减少1");
        } else {
            // 如果没找到有效移动，则跳过此测试
            System.out.println("未找到有效的测试移动，跳过测试");
        }
    }

    @Test
    public void testPerformInvalidMove() {
        // 设置状态为游戏进行中
        gameSession.setState(GameState.PLAYING);

        // 找到肯定不会匹配的移动（不同类型且无法形成匹配）
        Board board = gameSession.getBoard();
        int row1 = 0, col1 = 0;
        int row2 = 0, col2 = 1;

        // 确保这两个瓦片不会形成匹配
        String type1 = board.getTiles()[row1][col1].getType();
        String type2 = board.getTiles()[row2][col2].getType();

        // 如果类型相同，尝试找到不同类型的瓦片
        if (type1.equals(type2)) {
            boolean foundDifferent = false;
            for (int i = 0; i < board.getRows() && !foundDifferent; i++) {
                for (int j = 0; j < board.getColumns() - 1 && !foundDifferent; j++) {
                    String typeA = board.getTiles()[i][j].getType();
                    String typeB = board.getTiles()[i][j+1].getType();

                    if (!typeA.equals(typeB)) {
                        row1 = i;
                        col1 = j;
                        row2 = i;
                        col2 = j + 1;
                        foundDifferent = true;
                    }
                }
            }
        }

        // 临时交换并确认不会形成匹配
        String originalType1 = board.getTiles()[row1][col1].getType();
        String originalType2 = board.getTiles()[row2][col2].getType();

        board.getTiles()[row1][col1].setType(originalType2);
        board.getTiles()[row2][col2].setType(originalType1);

        boolean wouldMatch = hasPossibleMatch(board);

        // 恢复原状
        board.getTiles()[row1][col1].setType(originalType1);
        board.getTiles()[row2][col2].setType(originalType2);

        if (!wouldMatch) {
            int initialScore = board.getScore();
            int initialMoves = board.getMovesLeft();

            boolean moveResult = gameService.makeMove(gameSession, row1, col1, row2, col2);

            assertFalse(moveResult, "无效移动应返回false");
            assertEquals(initialScore, board.getScore(), "无效移动不应改变分数");
            assertEquals(initialMoves, board.getMovesLeft(), "无效移动不应减少移动次数");
        } else {
            System.out.println("未找到无效的测试移动，跳过测试");
        }
    }

    @Test
    public void testGameOver() {
        // 测试游戏结束逻辑
        Board board = gameSession.getBoard();
        gameSession.setState(GameState.PLAYING);

        // 强制设置剩余移动次数为1
        board.setMovesLeft(1);

        // 执行任意有效移动
        boolean foundMove = false;
        for (int i = 0; i < board.getRows() && !foundMove; i++) {
            for (int j = 0; j < board.getColumns() - 1 && !foundMove; j++) {
                // 临时交换相邻的瓦片看是否形成匹配
                String type1 = board.getTiles()[i][j].getType();
                String type2 = board.getTiles()[i][j+1].getType();

                board.getTiles()[i][j].setType(type2);
                board.getTiles()[i][j+1].setType(type1);

                if (hasPossibleMatch(board)) {
                    // 恢复原状
                    board.getTiles()[i][j].setType(type1);
                    board.getTiles()[i][j+1].setType(type2);

                    // 执行移动
                    gameService.makeMove(gameSession, i, j, i, j+1);
                    foundMove = true;
                } else {
                    // 恢复原状
                    board.getTiles()[i][j].setType(type1);
                    board.getTiles()[i][j+1].setType(type2);
                }
            }
        }

        if (foundMove) {
            // 验证游戏已结束
            assertEquals(0, board.getMovesLeft(), "移动次数应为0");
            assertTrue(board.isGameOver(), "游戏应处于结束状态");
            assertEquals(GameState.GAME_OVER, gameSession.getState(), "游戏状态应为GAME_OVER");
        } else {
            System.out.println("未找到有效的测试移动，跳过测试");
        }
    }

    // 辅助方法：检查是否有可能的匹配
    private boolean hasPossibleMatch(Board board) {
        // 水平检查
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns() - 2; j++) {
                if (board.getTiles()[i][j] != null &&
                        board.getTiles()[i][j+1] != null &&
                        board.getTiles()[i][j+2] != null &&
                        board.getTiles()[i][j].getType().equals(board.getTiles()[i][j+1].getType()) &&
                        board.getTiles()[i][j].getType().equals(board.getTiles()[i][j+2].getType())) {
                    return true;
                }
            }
        }

        // 垂直检查
        for (int i = 0; i < board.getRows() - 2; i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                if (board.getTiles()[i][j] != null &&
                        board.getTiles()[i+1][j] != null &&
                        board.getTiles()[i+2][j] != null &&
                        board.getTiles()[i][j].getType().equals(board.getTiles()[i+1][j].getType()) &&
                        board.getTiles()[i][j].getType().equals(board.getTiles()[i+2][j].getType())) {
                    return true;
                }
            }
        }

        return false;
    }
}