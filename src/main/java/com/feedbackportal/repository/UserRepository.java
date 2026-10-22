package com.feedbackportal.repository;

import com.feedbackportal.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    /** All user_ids for role USER (excluding admin account), sorted alphabetically */
    @Query("SELECT u.userId FROM User u WHERE u.role = 'USER' AND u.userId != :adminId ORDER BY u.userId ASC")
    List<String> findAllUserIds(@Param("adminId") String adminId, Pageable pageable);

    /** Filtered user_ids for role USER by pattern (case-insensitive contains match), sorted alphabetically */
    @Query("SELECT u.userId FROM User u WHERE u.role = 'USER' AND u.userId != :adminId AND LOWER(u.userId) LIKE :pattern ORDER BY u.userId ASC")
    List<String> findUserIdsMatching(@Param("pattern") String pattern, @Param("adminId") String adminId, Pageable pageable);
}

