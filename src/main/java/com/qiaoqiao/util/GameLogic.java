// 游戏逻辑工具类
package com.qiaoqiao.util;

import com.qiaoqiao.model.game.Board;
import com.qiaoqiao.model.game.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {

    // 检查是否存在匹配
    public static boolean hasMatches(Board board) {
        Tile[][] tiles = board.getTiles();
        int rows = board.getRows();
        int columns = board.getColumns();

        // 水平检查
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns - 2; j++) {
                if (tiles[i][j] != null && tiles[i][j+1] != null && tiles[i][j+2] != null &&
                        tiles[i][j].getType().equals(tiles[i][j+1].getType()) &&
                        tiles[i][j].getType().equals(tiles[i][j+2].getType())) {
                    return true;
                }
            }
        }

        // 垂直检查
        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < columns; j++) {
                if (tiles[i][j] != null && tiles[i+1][j] != null && tiles[i+2][j] != null &&
                        tiles[i][j].getType().equals(tiles[i+1][j].getType()) &&
                        tiles[i][j].getType().equals(tiles[i+2][j].getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    // 查找所有匹配
    public static List<List<int[]>> findAllMatches(Board board) {
        Tile[][] tiles = board.getTiles();
        int rows = board.getRows();
        int columns = board.getColumns();
        List<List<int[]>> allMatches = new ArrayList<>();

        // 水平匹配
        for (int i = 0; i < rows; i++) {
            int j = 0;
            while (j < columns - 2) {
                if (tiles[i][j] != null && tiles[i][j+1] != null && tiles[i][j+2] != null &&
                        tiles[i][j].getType().equals(tiles[i][j+1].getType()) &&
                        tiles[i][j].getType().equals(tiles[i][j+2].getType())) {

                    // 找到匹配开始处
                    String type = tiles[i][j].getType();
                    List<int[]> match = new ArrayList<>();
                    match.add(new int[]{i, j});

                    // 继续检查是否有更多连续匹配
                    int endJ = j + 2;
                    for (int k = j + 3; k < columns; k++) {
                        if (tiles[i][k] != null && tiles[i][k].getType().equals(type)) {
                            endJ = k;
                        } else {
                            break;
                        }
                    }

                    // 添加所有匹配的位置
                    for (int k = j + 1; k <= endJ; k++) {
                        match.add(new int[]{i, k});
                    }

                    allMatches.add(match);
                    j = endJ + 1;
                } else {
                    j++;
                }
            }
        }

        // 垂直匹配
        for (int j = 0; j < columns; j++) {
            int i = 0;
            while (i < rows - 2) {
                if (tiles[i][j] != null && tiles[i+1][j] != null && tiles[i+2][j] != null &&
                        tiles[i][j].getType().equals(tiles[i+1][j].getType()) &&
                        tiles[i][j].getType().equals(tiles[i+2][j].getType())) {

                    // 找到匹配开始处
                    String type = tiles[i][j].getType();
                    List<int[]> match = new ArrayList<>();
                    match.add(new int[]{i, j});

                    // 继续检查是否有更多连续匹配
                    int endI = i + 2;
                    for (int k = i + 3; k < rows; k++) {
                        if (tiles[k][j] != null && tiles[k][j].getType().equals(type)) {
                            endI = k;
                        } else {
                            break;
                        }
                    }

                    // 添加所有匹配的位置
                    for (int k = i + 1; k <= endI; k++) {
                        match.add(new int[]{k, j});
                    }

                    allMatches.add(match);
                    i = endI + 1;
                } else {
                    i++;
                }
            }
        }

        return allMatches;
    }

    // 检查是否有可能的移动
    public static boolean hasPossibleMoves(Board board) {
        Tile[][] tiles = board.getTiles();
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
    private static boolean checkPotentialMatch(Board board, int row1, int col1, int row2, int col2) {
        Tile[][] tiles = board.getTiles();

        // 获取瓦片类型
        if (tiles[row1][col1] == null || tiles[row2][col2] == null) {
            return false;
        }

        String type1 = tiles[row1][col1].getType();
        String type2 = tiles[row2][col2].getType();

        // 临时交换瓦片
        tiles[row1][col1].setType(type2);
        tiles[row2][col2].setType(type1);

        boolean hasMatch = hasMatches(board);

        // 交换回来
        tiles[row1][col1].setType(type1);
        tiles[row2][col2].setType(type2);

        return hasMatch;
    }

    // 创建特殊瓦片
    public static Tile createSpecialTile(int id, String type, int matchLength) {
        String imageUrl = Constants.PATH_TILES + type + ".png";

        if (matchLength == 4) {
            return new Tile(id, type, imageUrl, Constants.EFFECT_ROW_CLEAR);
        } else if (matchLength == 5) {
            return new Tile(id, type, imageUrl, Constants.EFFECT_COLOR_BOMB);
        } else if (matchLength > 5) {
            return new Tile(id, type, imageUrl, Constants.EFFECT_BOMB);
        }

        return new Tile(id, type, imageUrl);
    }

    // 计算匹配分数
    public static int calculateMatchScore(List<int[]> match) {
        int baseScore = match.size() * Constants.SCORE_PER_TILE;

        if (match.size() == 4) {
            return baseScore * Constants.SCORE_MULTIPLIER_4_MATCH;
        } else if (match.size() >= 5) {
            return baseScore * Constants.SCORE_MULTIPLIER_5_MATCH;
        }

        return baseScore;
    }

    // 随机生成瓦片类型
    public static String getRandomTileType() {
        Random random = new Random();
        return Constants.TILE_TYPES[random.nextInt(Constants.TILE_TYPES.length)];
    }

    // 重新洗牌面板
    public static void shuffleBoard(Board board) {
        Tile[][] tiles = board.getTiles();
        int rows = board.getRows();
        int columns = board.getColumns();
        Random random = new Random();

        // 随机重新分配瓦片类型
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (tiles[i][j] != null) {
                    String type = Constants.TILE_TYPES[random.nextInt(Constants.TILE_TYPES.length)];
                    String imageUrl = Constants.PATH_TILES + type + ".png";
                    tiles[i][j].setType(type);
                    tiles[i][j].setImageUrl(imageUrl);
                    tiles[i][j].setSpecial(false);
                    tiles[i][j].setSpecialEffect(null);
                }
            }
        }

        // 确保没有初始匹配
        while (hasMatches(board)) {
            shuffleBoard(board);
        }
    }

    // 获取特殊瓦片效果区域
    public static List<int[]> getSpecialEffectArea(Board board, int row, int col, String effect) {
        List<int[]> affectedTiles = new ArrayList<>();
        int rows = board.getRows();
        int columns = board.getColumns();

        switch (effect) {
            case Constants.EFFECT_ROW_CLEAR:
                // 整行效果
                for (int j = 0; j < columns; j++) {
                    affectedTiles.add(new int[]{row, j});
                }
                break;

            case Constants.EFFECT_COLUMN_CLEAR:
                // 整列效果
                for (int i = 0; i < rows; i++) {
                    affectedTiles.add(new int[]{i, col});
                }
                break;

            case Constants.EFFECT_BOMB:
                // 3x3 爆炸效果
                for (int i = Math.max(0, row - 1); i <= Math.min(rows - 1, row + 1); i++) {
                    for (int j = Math.max(0, col - 1); j <= Math.min(columns - 1, col + 1); j++) {
                        affectedTiles.add(new int[]{i, j});
                    }
                }
                break;

            case Constants.EFFECT_COLOR_BOMB:
                // 所有同色瓦片效果
                Tile[][] tiles = board.getTiles();
                String targetType = tiles[row][col].getType();

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        if (tiles[i][j] != null && tiles[i][j].getType().equals(targetType)) {
                            affectedTiles.add(new int[]{i, j});
                        }
                    }
                }
                break;
        }

        return affectedTiles;
    }
}