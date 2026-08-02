package com.securevault.dto.response;

public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    public JwtAuthResponse() {}

    public JwtAuthResponse(String accessToken, String tokenType, UserResponse user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public static JwtAuthResponseBuilder builder() { return new JwtAuthResponseBuilder(); }

    public static class JwtAuthResponseBuilder {
        private String accessToken;
        private String tokenType = "Bearer";
        private UserResponse user;

        public JwtAuthResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public JwtAuthResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public JwtAuthResponseBuilder user(UserResponse user) { this.user = user; return this; }

        public JwtAuthResponse build() {
            return new JwtAuthResponse(accessToken, tokenType, user);
        }
    }
}
