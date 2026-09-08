package com.securevault.repository;

import com.securevault.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.userEmail = :email AND l.loginStatus = 'FAILED' AND l.createdAt >= :since")
    long countFailedLoginsInTimeWindow(@Param("email") String email, @Param("since") ZonedDateTime since);

    @Query("SELECT MAX(l.createdAt) FROM LoginLog l WHERE l.userEmail = :email AND l.loginStatus = 'SUCCESS'")
    ZonedDateTime findLatestSuccessTimeByUserEmail(@Param("email") String email);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.userEmail = :email AND l.loginStatus = 'FAILED' AND l.createdAt > :since")
    long countFailedLoginsSince(@Param("email") String email, @Param("since") ZonedDateTime since);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.userEmail = :email AND l.loginStatus = 'FAILED'")
    long countAllFailedLoginsByUserEmail(@Param("email") String email);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.userEmail = :email AND (l.isRead IS NULL OR l.isRead = false)")
    long countUnreadByUserEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE LoginLog l SET l.isRead = true WHERE l.userEmail = :email AND (l.isRead IS NULL OR l.isRead = false)")
    void markAllAsReadByUserEmail(@Param("email") String email);
}
