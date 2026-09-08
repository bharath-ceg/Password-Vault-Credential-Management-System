package com.securevault.repository;

import com.securevault.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserEmailOrderByTimestampDesc(String userEmail);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userEmail = :email AND (a.isRead IS NULL OR a.isRead = false)")
    long countUnreadByUserEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE AuditLog a SET a.isRead = true WHERE a.userEmail = :email AND (a.isRead IS NULL OR a.isRead = false)")
    void markAllAsReadByUserEmail(@Param("email") String email);
}
