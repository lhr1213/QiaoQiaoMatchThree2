// 分数仓库
package com.qiaoqiao.repository;

import com.qiaoqiao.model.Score;
import com.qiaoqiao.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByUser(User user);

    @Query("SELECT s FROM Score s ORDER BY s.score DESC")
    List<Score> findAllOrderByScoreDesc();

    @Query(value = "SELECT * FROM scores ORDER BY score DESC LIMIT ?1", nativeQuery = true)
    List<Score> findTopNScores(int limit);

    @Query("SELECT s FROM Score s WHERE s.user = ?1 ORDER BY s.score DESC")
    List<Score> findUserScoresOrderByScoreDesc(User user);

    @Query(value = "SELECT * FROM scores WHERE user_id = ?1 ORDER BY score DESC LIMIT ?2", nativeQuery = true)
    List<Score> findTopNScoresByUser(Long userId, int limit);

    @Query("SELECT s FROM Score s WHERE s.createdAt >= ?1")
    List<Score> findScoresAfterDate(LocalDateTime date);

    @Query("SELECT s FROM Score s WHERE s.gameMode = ?1 ORDER BY s.score DESC")
    List<Score> findTopScoresByGameMode(String gameMode);

    @Query("SELECT AVG(s.score) FROM Score s WHERE s.user = ?1")
    Double findAverageScoreByUser(User user);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.user = ?1")
    Long countScoresByUser(User user);
}