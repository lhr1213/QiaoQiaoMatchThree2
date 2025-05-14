// 用户仓库
package com.qiaoqiao.repository;

import com.qiaoqiao.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u ORDER BY u.highestScore DESC")
    List<User> findTopUsersByHighestScore();

    @Query(value = "SELECT * FROM users ORDER BY highest_score DESC LIMIT ?1", nativeQuery = true)
    List<User> findTopNUsersByHighestScore(int limit);

    @Query("SELECT u FROM User u WHERE u.lastLogin > CURRENT_DATE - 7")
    List<User> findRecentlyActiveUsers();
}