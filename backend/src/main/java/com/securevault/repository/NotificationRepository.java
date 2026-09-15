package com.securevault.repository;

import com.securevault.entity.Notification;
import com.securevault.entity.User;
import com.securevault.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    long countByUserAndIsReadFalse(User user);

    long countByUserEmailAndIsReadFalse(String userEmail);

    boolean existsByUserAndTypeAndReferenceId(User user, NotificationType type, String referenceId);

    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE LOWER(n.user.email) = LOWER(:email) AND n.type = :type AND n.referenceId = :referenceId")
    boolean existsByUserEmailAndTypeAndReferenceId(@Param("email") String email, @Param("type") NotificationType type, @Param("referenceId") String referenceId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE LOWER(n.user.email) = LOWER(:email) AND n.isRead = false")
    void markAllAsReadByUserEmail(@Param("email") String email);
}
