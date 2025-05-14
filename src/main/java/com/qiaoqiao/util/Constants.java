// 常量类
package com.qiaoqiao.util;

public class Constants {

    // 游戏配置
    public static final int DEFAULT_BOARD_SIZE = 8;
    public static final int DEFAULT_MOVES = 20;
    public static final int MIN_MATCH_LENGTH = 3;

    // 瓦片类型
    public static final String[] TILE_TYPES = {"红色", "蓝色", "绿色", "黄色", "紫色", "白色", "黑色", "棕色", "橙色"};

    // 特殊瓦片效果
    public static final String EFFECT_ROW_CLEAR = "row_clear";
    public static final String EFFECT_COLUMN_CLEAR = "column_clear";
    public static final String EFFECT_BOMB = "bomb";
    public static final String EFFECT_COLOR_BOMB = "color_bomb";

    // 游戏模式
    public static final String MODE_CLASSIC = "classic";
    public static final String MODE_TIMED = "timed";
    public static final String MODE_CHALLENGE = "challenge";

    // 游戏状态
    public static final String STATE_READY = "ready";
    public static final String STATE_PLAYING = "playing";
    public static final String STATE_PAUSED = "paused";
    public static final String STATE_GAME_OVER = "game_over";

    // WebSocket消息类型
    public static final String MESSAGE_JOIN = "join";
    public static final String MESSAGE_MOVE = "move";
    public static final String MESSAGE_GAME_STATE = "gameState";
    public static final String MESSAGE_GAME_OVER = "gameOver";
    public static final String MESSAGE_ERROR = "error";
    public static final String MESSAGE_NOTIFICATION = "notification";

    // 分数配置
    public static final int SCORE_PER_TILE = 10;
    public static final int SCORE_MULTIPLIER_4_MATCH = 2;
    public static final int SCORE_MULTIPLIER_5_MATCH = 3;

    // 动画配置
    public static final int ANIMATION_SWAP_DURATION = 300;  // 毫秒
    public static final int ANIMATION_MATCH_DURATION = 500;  // 毫秒
    public static final int ANIMATION_DROP_DURATION = 500;  // 毫秒

    // 会话属性
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_GAME_SESSION = "gameSession";

    // 路径配置
    public static final String PATH_IMAGES = "/images/";
    public static final String PATH_TILES = "/images/tiles/";
    public static final String PATH_AUDIO = "/audio/";

    // 其他配置
    public static final int LEADERBOARD_DEFAULT_LIMIT = 20;
    public static final int USER_TOP_SCORES_LIMIT = 10;
}