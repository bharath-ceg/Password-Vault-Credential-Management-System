package com.securevault.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "login_logs", indexes = {
    @Index(name = "idx_login_user_email", columnList = "user_email"),
    @Index(name = "idx_login_created_at", columnList = "created_at")
})
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(name = "login_status", nullable = false, length = 20)
    private String loginStatus; // SUCCESS / FAILED

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    public LoginLog() {}

    public LoginLog(Long id, User user, String userName, String userEmail, String loginStatus, Boolean isRead, ZonedDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.userName = userName;
        this.userEmail = userEmail;
        this.loginStatus = loginStatus;
        this.isRead = isRead != null ? isRead : false;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = ZonedDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getLoginStatus() { return loginStatus; }
    public void setLoginStatus(String loginStatus) { this.loginStatus = loginStatus; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static LoginLogBuilder builder() { return new LoginLogBuilder(); }

    public static class LoginLogBuilder {
        private Long id;
        private User user;
        private String userName;
        private String userEmail;
        private String loginStatus;
        private Boolean isRead = false;
        private ZonedDateTime createdAt;

        public LoginLogBuilder id(Long id) { this.id = id; return this; }
        public LoginLogBuilder user(User user) { this.user = user; return this; }
        public LoginLogBuilder userName(String userName) { this.userName = userName; return this; }
        public LoginLogBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public LoginLogBuilder loginStatus(String loginStatus) { this.loginStatus = loginStatus; return this; }
        public LoginLogBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public LoginLogBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LoginLog build() {
            return new LoginLog(id, user, userName, userEmail, loginStatus, isRead, createdAt);
        }
    }
}
