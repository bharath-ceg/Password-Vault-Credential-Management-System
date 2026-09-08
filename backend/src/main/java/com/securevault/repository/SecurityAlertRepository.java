package com.securevault.repository;

import com.securevault.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {

    List<SecurityAlert> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<SecurityAlert> findByUserEmailAndStatusOrderByCreatedAtDesc(String userEmail, String status);

    @Query("SELECT a FROM SecurityAlert a WHERE a.userEmail = :email AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<SecurityAlert> findActiveAlertsByUserEmail(@Param("email") String email, @Param("since") ZonedDateTime since);

    @Query("SELECT COUNT(a) FROM SecurityAlert a WHERE a.userEmail = :email AND (a.isRead IS NULL OR a.isRead = false) AND a.status = 'UNREAD' AND a.createdAt >= :since")
    long countActiveUnreadByUserEmail(@Param("email") String email, @Param("since") ZonedDateTime since);

    @Query("SELECT COUNT(a) FROM SecurityAlert a WHERE a.userEmail = :email AND (a.isRead IS NULL OR a.isRead = false) AND a.status = 'UNREAD'")
    long countUnreadByUserEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE SecurityAlert a SET a.isRead = true, a.status = 'READ' WHERE a.userEmail = :email AND (a.isRead IS NULL OR a.isRead = false)")
    void markAllAsReadByUserEmail(@Param("email") String email);
}
