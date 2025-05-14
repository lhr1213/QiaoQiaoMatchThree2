// 游戏移动类
package com.qiaoqiao.model.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示游戏中的一次移动操作
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Move {

    // 第一个瓦片的行坐标
    private int row1;

    // 第一个瓦片的列坐标
    private int col1;

    // 第二个瓦片的行坐标
    private int row2;

    // 第二个瓦片的列坐标
    private int col2;

    // 移动是否成功
    private boolean successful;

    // 移动后匹配的瓦片数量
    private int matchedTiles;

    // 获得的分数
    private int score;

    // 是否触发特殊效果
    private boolean specialEffect;

    // 特殊效果类型
    private String specialEffectType;

    /**
     * 创建基本移动对象（不包含结果信息）
     */
    public Move(int row1, int col1, int row2, int col2) {
        this.row1 = row1;
        this.col1 = col1;
        this.row2 = row2;
        this.col2 = col2;
        this.successful = false;
        this.matchedTiles = 0;
        this.score = 0;
        this.specialEffect = false;
        this.specialEffectType = null;
    }

    /**
     * 创建包含结果信息的移动对象
     */
    public Move(int row1, int col1, int row2, int col2, boolean successful, int matchedTiles, int score) {
        this.row1 = row1;
        this.col1 = col1;
        this.row2 = row2;
        this.col2 = col2;
        this.successful = successful;
        this.matchedTiles = matchedTiles;
        this.score = score;
        this.specialEffect = false;
        this.specialEffectType = null;
    }

    /**
     * 检查两个瓦片是否相邻
     */
    public boolean isAdjacent() {
        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
                (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    /**
     * 设置特殊效果信息
     */
    public void setSpecialEffectInfo(String effectType) {
        this.specialEffect = true;
        this.specialEffectType = effectType;
    }

    /**
     * 获取移动方向
     * @return 移动方向：UP, DOWN, LEFT, RIGHT 或 INVALID
     */
    public String getDirection() {
        if (row1 == row2) {
            if (col2 > col1) {
                return "RIGHT";
            } else if (col2 < col1) {
                return "LEFT";
            }
        } else if (col1 == col2) {
            if (row2 > row1) {
                return "DOWN";
            } else if (row2 < row1) {
                return "UP";
            }
        }
        return "INVALID";
    }

    /**
     * 检查移动是否有效（瓦片坐标在有效范围内）
     */
    public boolean isValid(int rows, int columns) {
        return row1 >= 0 && row1 < rows &&
                col1 >= 0 && col1 < columns &&
                row2 >= 0 && row2 < rows &&
                col2 >= 0 && col2 < columns &&
                isAdjacent();
    }
}