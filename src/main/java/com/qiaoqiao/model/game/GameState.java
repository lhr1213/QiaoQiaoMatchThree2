// 游戏状态枚举
package com.qiaoqiao.model.game;

/**
 * 定义游戏的不同状态
 */
public enum GameState {

    /**
     * 准备开始状态
     * 游戏已初始化但尚未开始
     */
    READY("ready"),

    /**
     * 游戏进行中状态
     * 游戏已开始且正在进行
     */
    PLAYING("playing"),

    /**
     * 游戏暂停状态
     * 游戏已暂停，可以继续
     */
    PAUSED("paused"),

    /**
     * 游戏结束状态
     * 游戏已结束，无法继续
     */
    GAME_OVER("game_over");

    private final String value;

    GameState(String value) {
        this.value = value;
    }

    /**
     * 获取状态的字符串值
     */
    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值获取对应的枚举
     */
    public static GameState fromValue(String value) {
        for (GameState state : GameState.values()) {
            if (state.value.equals(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("未知的游戏状态: " + value);
    }

    /**
     * 检查当前状态是否可以进行操作
     */
    public boolean canPerformActions() {
        return this == PLAYING;
    }

    /**
     * 检查游戏是否已结束
     */
    public boolean isGameOver() {
        return this == GAME_OVER;
    }

    /**
     * 检查游戏是否可以重新开始
     */
    public boolean canRestart() {
        return this != PLAYING;
    }

    /**
     * 检查游戏是否可以暂停
     */
    public boolean canPause() {
        return this == PLAYING;
    }

    /**
     * 检查游戏是否可以继续
     */
    public boolean canResume() {
        return this == PAUSED;
    }

    /**
     * 获取下一个状态（根据游戏流程）
     */
    public GameState getNextState() {
        switch (this) {
            case READY:
                return PLAYING;
            case PLAYING:
                return PAUSED;
            case PAUSED:
                return PLAYING;
            case GAME_OVER:
                return READY;
            default:
                return this;
        }
    }
}