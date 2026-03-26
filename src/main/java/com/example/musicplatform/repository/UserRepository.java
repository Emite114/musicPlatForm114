package com.example.musicplatform.repository;

import com.example.musicplatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
