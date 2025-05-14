// 分数模型
package com.qiaoqiao.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String gameMode;

    private int boardSize;

    private int movesUsed;

    private boolean completed;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 构造函数
    public Score() {
    }

    public Score(User user, int score) {
        this.user = user;
        this.score = score;
        this.createdAt = LocalDateTime.now();
        this.gameMode = "classic";
        this.boardSize = 8;
        this.completed = true;
    }

    public Score(User user, int score, int boardSize, int movesUsed, boolean completed) {
        this.user = user;
        this.score = score;
        this.createdAt = LocalDateTime.now();
        this.gameMode = "classic";
        this.boardSize = boardSize;
        this.movesUsed = movesUsed;
        this.completed = completed;
    }
}