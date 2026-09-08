package com.securevault.dto.response;

import java.util.ArrayList;
import java.util.List;

public class PasswordGenerationResponse {

    private String password;
    private int strengthScore;
    private String strengthLabel;
    private List<String> suggestions = new ArrayList<>();

    public PasswordGenerationResponse() {}

    public PasswordGenerationResponse(String password, int strengthScore, String strengthLabel, List<String> suggestions) {
        this.password = password;
        this.strengthScore = strengthScore;
        this.strengthLabel = strengthLabel;
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getStrengthScore() { return strengthScore; }
    public void setStrengthScore(int strengthScore) { this.strengthScore = strengthScore; }

    public String getStrengthLabel() { return strengthLabel; }
    public void setStrengthLabel(String strengthLabel) { this.strengthLabel = strengthLabel; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public static PasswordGenerationResponseBuilder builder() { return new PasswordGenerationResponseBuilder(); }

    public static class PasswordGenerationResponseBuilder {
        private String password;
        private int strengthScore;
        private String strengthLabel;
        private List<String> suggestions = new ArrayList<>();

        public PasswordGenerationResponseBuilder password(String password) { this.password = password; return this; }
        public PasswordGenerationResponseBuilder strengthScore(int strengthScore) { this.strengthScore = strengthScore; return this; }
        public PasswordGenerationResponseBuilder strengthLabel(String strengthLabel) { this.strengthLabel = strengthLabel; return this; }
        public PasswordGenerationResponseBuilder suggestions(List<String> suggestions) { this.suggestions = suggestions; return this; }

        public PasswordGenerationResponse build() {
            return new PasswordGenerationResponse(password, strengthScore, strengthLabel, suggestions);
        }
    }
}
