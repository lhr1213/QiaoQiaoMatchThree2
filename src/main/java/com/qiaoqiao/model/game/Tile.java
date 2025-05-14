// 游戏瓦片模型
package com.qiaoqiao.model.game;

import lombok.Data;

@Data
public class Tile {
    private int id;          // 瓦片ID
    private String type;     // 瓦片类型（例如：红色、蓝色、绿色等）
    private String imageUrl; // 瓦片图片URL
    private boolean isSpecial; // 是否是特殊瓦片
    private String specialEffect; // 特殊效果（如果有）

    // 构造函数
    public Tile(int id, String type, String imageUrl) {
        this.id = id;
        this.type = type;
        this.imageUrl = imageUrl;
        this.isSpecial = false;
        this.specialEffect = null;
    }

    // 特殊瓦片构造函数
    public Tile(int id, String type, String imageUrl, String specialEffect) {
        this.id = id;
        this.type = type;
        this.imageUrl = imageUrl;
        this.isSpecial = true;
        this.specialEffect = specialEffect;
    }
}