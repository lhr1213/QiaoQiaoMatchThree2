// 分数服务
package com.qiaoqiao.service;

import com.qiaoqiao.model.Score;
import com.qiaoqiao.model.User;
import com.qiaoqiao.repository.ScoreRepository;
import com.qiaoqiao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private UserRepository userRepository;

    // 保存分数记录
    @Transactional
    public Score saveScore(Long userId, int score) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 创建分数记录
            Score scoreRecord = new Score(user, score);

            // 更新用户最高分
            if (score > user.getHighestScore()) {
                user.setHighestScore(score);
                userRepository.save(user);
            }

            // 增加用户游戏次数
            user.incrementGamesCount();
            userRepository.save(user);

            return scoreRepository.save(scoreRecord);
        }

        throw new RuntimeException("用户不存在");
    }

    // 保存分数记录（游客）
    public Score saveGuestScore(int score) {
        Score scoreRecord = new Score();
        scoreRecord.setScore(score);
        scoreRecord.setCreatedAt(LocalDateTime.now());
        scoreRecord.setGameMode("classic");
        scoreRecord.setBoardSize(8);
        scoreRecord.setCompleted(true);

        return scoreRepository.save(scoreRecord);
    }

    // 保存完整分数记录
    @Transactional
    public Score saveDetailedScore(Long userId, int score, int boardSize, int movesUsed, boolean completed) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 创建分数记录
            Score scoreRecord = new Score(user, score, boardSize, movesUsed, completed);

            // 如果游戏完成且分数更高，更新用户最高分
            if (completed && score > user.getHighestScore()) {
                user.setHighestScore(score);
                userRepository.save(user);
            }

            // 增加用户游戏次数
            user.incrementGamesCount();
            userRepository.save(user);

            return scoreRepository.save(scoreRecord);
        }

        throw new RuntimeException("用户不存在");
    }

    // 获取用户所有分数
    public List<Score> getUserScores(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return scoreRepository.findUserScoresOrderByScoreDesc(user);
        }

        throw new RuntimeException("用户不存在");
    }

    // 获取用户最高分数
    public List<Score> getUserTopScores(Long userId, int limit) {
        return scoreRepository.findTopNScoresByUser(userId, limit);
    }

    // 获取全局排行榜
    public List<Score> getLeaderboard(int limit) {
        return scoreRepository.findTopNScores(limit);
    }

    // 获取特定游戏模式的排行榜
    public List<Score> getLeaderboardByGameMode(String gameMode) {
        return scoreRepository.findTopScoresByGameMode(gameMode);
    }

    // 获取最近分数记录
    public List<Score> getRecentScores(int days) {
        LocalDateTime date = LocalDateTime.now().minusDays(days);
        return scoreRepository.findScoresAfterDate(date);
    }

    // 获取用户平均分数
    public Double getUserAverageScore(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return scoreRepository.findAverageScoreByUser(user);
        }

        throw new RuntimeException("用户不存在");
    }

    // 获取用户游戏次数
    public Long getUserGamesCount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return scoreRepository.countScoresByUser(user);
        }

        throw new RuntimeException("用户不存在");
    }

    // 删除分数记录
    public void deleteScore(Long scoreId) {
        scoreRepository.deleteById(scoreId);
    }

    // 获取单条分数记录
    public Score getScoreById(Long scoreId) {
        return scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("分数记录不存在"));
    }
}