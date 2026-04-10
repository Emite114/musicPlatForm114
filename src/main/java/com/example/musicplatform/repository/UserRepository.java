package com.example.musicplatform.repository;

import com.example.musicplatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);
    @Query("""
    SELECT u FROM User u
    WHERE (:keyword IS NULL OR u.username LIKE %:keyword%)
    ORDER BY u.id DESC
""")
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.followCount = u.followCount+1 WHERE u.id= :userId")
    void increaseFollowCountByUserId(Long userId);

    @Modifying
    @Query("UPDATE User u SET u.followCount = u.followCount-1 WHERE u.id = :userId ")
    void decreaseFollowCountByUserId(Long userId);
    @Modifying
    @Query("UPDATE User u SET u.fanCount = u.fanCount+1 WHERE u.id= :userId")
    void increaseFanCountByUserId(Long userId);
    @Modifying
    @Query("UPDATE User u SET u.fanCount = u.fanCount-1 WHERE u.id= :userId")
    void decreaseFanCountByUserId(Long userId);

    @Query("SELECT u FROM User u WHERE u.id IN" + "(SELECT f.followUserId FROM Follow f WHERE f.userId = :userId)" )
    Page<User> findFollows(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id IN" + "(SELECT f.userId FROM Follow f WHERE f.followUserId = :userId)" )
    Page<User> findFans(@Param("userId") Long userId, Pageable pageable);

//    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN (SELECT f.userId FROM Follow f WHERE f.followUserId = :userId)")
//    Long countFans(@Param("userId") Long userId);
//
//    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN (SELECT f.followUserId FROM Follow f WHERE f.userId = :userId)")
//    Long countFollows(@Param("userId") Long userId);
}
