package com.securevault.repository;

import com.securevault.entity.SuspiciousActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivity, Long> {

    List<SuspiciousActivity> findByUserEmailOrderByDetectedAtDesc(String userEmail);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SuspiciousActivity s WHERE s.userEmail = :email AND s.status = 'FLAGGED' AND s.detectedAt >= :since")
    boolean existsRecentFlaggedActivity(@Param("email") String email, @Param("since") ZonedDateTime since);

    @Query("SELECT MAX(s.detectedAt) FROM SuspiciousActivity s WHERE s.userEmail = :email")
    ZonedDateTime findLatestDetectedTimeByUserEmail(@Param("email") String email);

    @Query("SELECT COUNT(s) FROM SuspiciousActivity s WHERE s.userEmail = :email AND (s.isRead IS NULL OR s.isRead = false)")
    long countUnreadByUserEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE SuspiciousActivity s SET s.isRead = true, s.status = 'READ' WHERE s.userEmail = :email AND (s.isRead IS NULL OR s.isRead = false)")
    void markAllAsReadByUserEmail(@Param("email") String email);
}
