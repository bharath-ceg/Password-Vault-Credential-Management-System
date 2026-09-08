package com.securevault.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class LoginLogResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDate loginDate;
    private LocalTime loginTime;
    private String loginStatus;
    private ZonedDateTime createdAt;

    public LoginLogResponse() {}

    public LoginLogResponse(Long id, Long userId, String userName, String userEmail, LocalDate loginDate, LocalTime loginTime, String loginStatus, ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.loginDate = loginDate;
        this.loginTime = loginTime;
        this.loginStatus = loginStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDate getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDate loginDate) { this.loginDate = loginDate; }

    public LocalTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalTime loginTime) { this.loginTime = loginTime; }

    public String getLoginStatus() { return loginStatus; }
    public void setLoginStatus(String loginStatus) { this.loginStatus = loginStatus; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static LoginLogResponseBuilder builder() { return new LoginLogResponseBuilder(); }

    public static class LoginLogResponseBuilder {
        private Long id;
        private Long userId;
        private String userName;
        private String userEmail;
        private LocalDate loginDate;
        private LocalTime loginTime;
        private String loginStatus;
        private ZonedDateTime createdAt;

        public LoginLogResponseBuilder id(Long id) { this.id = id; return this; }
        public LoginLogResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public LoginLogResponseBuilder userName(String userName) { this.userName = userName; return this; }
        public LoginLogResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public LoginLogResponseBuilder loginDate(LocalDate loginDate) { this.loginDate = loginDate; return this; }
        public LoginLogResponseBuilder loginTime(LocalTime loginTime) { this.loginTime = loginTime; return this; }
        public LoginLogResponseBuilder loginStatus(String loginStatus) { this.loginStatus = loginStatus; return this; }
        public LoginLogResponseBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LoginLogResponse build() {
            return new LoginLogResponse(id, userId, userName, userEmail, loginDate, loginTime, loginStatus, createdAt);
        }
    }
}
