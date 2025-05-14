// 游戏面板模型
package com.qiaoqiao.model.game;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

@Data
public class Board {
    private int rows;              // 行数
    private int columns;           // 列数
    private Tile[][] tiles;        // 瓦片二维数组
    private int score;             // 当前分数
    private int movesLeft;         // 剩余移动次数
    // 在Board.java中修改TILE_TYPES数组
    private static final String[] TILE_TYPES = {"红色", "蓝色", "绿色", "黄色", "紫色", "白色", "黑色", "棕色", "橙色"};

    // 构造函数
    public Board(int rows, int columns, int movesLeft) {
        this.rows = rows;
        this.columns = columns;
        this.tiles = new Tile[rows][columns];
        this.score = 0;
        this.movesLeft = movesLeft;
        initializeBoard();
    }

    // 初始化游戏面板
    private void initializeBoard() {
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                String type = TILE_TYPES[random.nextInt(TILE_TYPES.length)];
                String imageUrl = "/images/tiles/" + type + ".png";
                tiles[i][j] = new Tile(i * columns + j, type, imageUrl);
            }
        }

        // 确保初始面板没有匹配的组合
        while (hasMatches()) {
            shuffleBoard();
        }
    }

    // 检查是否存在匹配组合
    public boolean hasMatches() {
        // 水平检查
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns - 2; j++) {
                if (tiles[i][j].getType().equals(tiles[i][j+1].getType()) &&
                        tiles[i][j].getType().equals(tiles[i][j+2].getType())) {
                    return true;
                }
            }
        }

        // 垂直检查
        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < columns; j++) {
                if (tiles[i][j].getType().equals(tiles[i+1][j].getType()) &&
                        tiles[i][j].getType().equals(tiles[i+2][j].getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    // 重新洗牌面板
    private void shuffleBoard() {
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                String type = TILE_TYPES[random.nextInt(TILE_TYPES.length)];
                String imageUrl = "/images/tiles/" + type + ".png";
                tiles[i][j] = new Tile(i * columns + j, type, imageUrl);
            }
        }
    }

    // 交换两个瓦片
    public boolean swapTiles(int row1, int col1, int row2, int col2) {
        // 检查是否相邻
        if (!areAdjacent(row1, col1, row2, col2)) {
            return false;
        }

        // 交换瓦片
        Tile temp = tiles[row1][col1];
        tiles[row1][col1] = tiles[row2][col2];
        tiles[row2][col2] = temp;

        // 检查交换后是否形成匹配
        if (checkMatches()) {
            // 减少移动次数
            movesLeft--;
            return true;
        } else {
            // 交换回来
            temp = tiles[row1][col1];
            tiles[row1][col1] = tiles[row2][col2];
            tiles[row2][col2] = temp;
            return false;
        }
    }

    // 检查两个瓦片是否相邻
    private boolean areAdjacent(int row1, int col1, int row2, int col2) {
        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
                (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    // 检查并处理匹配
    private boolean checkMatches() {
        List<Tile> matchedTiles = findMatches();
        if (matchedTiles.isEmpty()) {
            return false;
        }

        // 处理匹配的瓦片
        for (Tile tile : matchedTiles) {
            int row = tile.getId() / columns;
            int col = tile.getId() % columns;
            // 增加分数
            score += 10;
            // 将匹配的瓦片设置为null，之后会填充新瓦片
            tiles[row][col] = null;
        }

        // 填充新瓦片
        fillBoard();

        // 递归检查新的匹配
        if (hasMatches()) {
            checkMatches();
        }

        return true;
    }

    // 查找匹配的瓦片
    private List<Tile> findMatches() {
        List<Tile> matchedTiles = new ArrayList<>();

        // 水平匹配
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns - 2; j++) {
                if (tiles[i][j] != null && tiles[i][j+1] != null && tiles[i][j+2] != null &&
                        tiles[i][j].getType().equals(tiles[i][j+1].getType()) &&
                        tiles[i][j].getType().equals(tiles[i][j+2].getType())) {
                    if (!matchedTiles.contains(tiles[i][j])) matchedTiles.add(tiles[i][j]);
                    if (!matchedTiles.contains(tiles[i][j+1])) matchedTiles.add(tiles[i][j+1]);
                    if (!matchedTiles.contains(tiles[i][j+2])) matchedTiles.add(tiles[i][j+2]);
                }
            }
        }

        // 垂直匹配
        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < columns; j++) {
                if (tiles[i][j] != null && tiles[i+1][j] != null && tiles[i+2][j] != null &&
                        tiles[i][j].getType().equals(tiles[i+1][j].getType()) &&
                        tiles[i][j].getType().equals(tiles[i+2][j].getType())) {
                    if (!matchedTiles.contains(tiles[i][j])) matchedTiles.add(tiles[i][j]);
                    if (!matchedTiles.contains(tiles[i+1][j])) matchedTiles.add(tiles[i+1][j]);
                    if (!matchedTiles.contains(tiles[i+2][j])) matchedTiles.add(tiles[i+2][j]);
                }
            }
        }

        return matchedTiles;
    }

    // 填充面板中的空位
    private void fillBoard() {
        Random random = new Random();

        // 下落现有瓦片
        for (int j = 0; j < columns; j++) {
            for (int i = rows - 1; i >= 0; i--) {
                if (tiles[i][j] == null) {
                    // 查找上方最近的非空瓦片
                    for (int k = i - 1; k >= 0; k--) {
                        if (tiles[k][j] != null) {
                            tiles[i][j] = tiles[k][j];
                            tiles[k][j] = null;
                            break;
                        }
                    }
                }
            }
        }

        // 填充顶部空位
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (tiles[i][j] == null) {
                    String type = TILE_TYPES[random.nextInt(TILE_TYPES.length)];
                    String imageUrl = "/images/tiles/" + type + ".png";
                    tiles[i][j] = new Tile(i * columns + j, type, imageUrl);
                }
            }
        }
    }

    // 检查游戏是否结束
    public boolean isGameOver() {
        return movesLeft <= 0;
    }
}