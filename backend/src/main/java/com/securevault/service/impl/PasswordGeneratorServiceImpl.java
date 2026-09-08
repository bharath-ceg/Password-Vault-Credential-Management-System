package com.securevault.service.impl;

import com.securevault.dto.request.GeneratePasswordRequest;
import com.securevault.dto.response.PasswordGenerationResponse;
import com.securevault.exception.BadRequestException;
import com.securevault.service.PasswordGeneratorService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PasswordGeneratorServiceImpl implements PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private static final String[] COMMON_PATTERNS = {
        "123456", "password", "12345678", "qwerty", "123456789", "12345", "1234", "111111",
        "1234567", "dragon", "welcome", "admin", "abc123", "password1"
    };

    private final SecureRandom random = new SecureRandom();

    @Override
    public PasswordGenerationResponse generatePassword(GeneratePasswordRequest request) {
        if (!request.isUseUpper() && !request.isUseLower() && !request.isUseNumbers() && !request.isUseSpecial()) {
            throw new BadRequestException("At least one character set must be selected (Uppercase, Lowercase, Numbers, or Special characters).");
        }

        int length = request.getLength();
        if (length < 8) length = 8;
        if (length > 64) length = 64;

        StringBuilder charPool = new StringBuilder();
        List<Character> passwordChars = new ArrayList<>();

        if (request.isUseUpper()) {
            charPool.append(UPPERCASE);
            passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (request.isUseLower()) {
            charPool.append(LOWERCASE);
            passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (request.isUseNumbers()) {
            charPool.append(NUMBERS);
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (request.isUseSpecial()) {
            charPool.append(SPECIAL);
            passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        int remainingLength = length - passwordChars.size();
        for (int i = 0; i < remainingLength; i++) {
            passwordChars.add(charPool.charAt(random.nextInt(charPool.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder generated = new StringBuilder();
        for (char c : passwordChars) {
            generated.append(c);
        }

        String passwordStr = generated.toString();
        return analyzePassword(passwordStr);
    }

    @Override
    public PasswordGenerationResponse analyzePassword(String password) {
        if (password == null || password.isEmpty()) {
            List<String> suggestions = List.of("Password cannot be empty.");
            return PasswordGenerationResponse.builder()
                    .password("")
                    .strengthScore(0)
                    .strengthLabel("Weak")
                    .suggestions(suggestions)
                    .build();
        }

        int score = 0;
        List<String> suggestions = new ArrayList<>();

        // Length checks
        if (password.length() >= 8) {
            score += 20;
        } else {
            suggestions.add("Increase password length to at least 8 characters (12+ recommended).");
        }

        if (password.length() >= 12) {
            score += 15;
        } else if (password.length() >= 8) {
            suggestions.add("Make the password 12 characters or longer for stronger protection.");
        }

        if (password.length() >= 16) {
            score += 15;
        }

        // Character set checks
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNum = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

        if (hasUpper) score += 15; else suggestions.add("Add at least one uppercase letter (A-Z).");
        if (hasLower) score += 10; else suggestions.add("Add at least one lowercase letter (a-z).");
        if (hasNum) score += 15; else suggestions.add("Add at least one number (0-9).");
        if (hasSpecial) score += 10; else suggestions.add("Add at least one special character (!@#$%^&*).");

        // Weak pattern checks
        String lowerPass = password.toLowerCase();
        for (String pattern : COMMON_PATTERNS) {
            if (lowerPass.contains(pattern)) {
                score -= 25;
                suggestions.add("Avoid common dictionary words or predictable sequences like '" + pattern + "'.");
                break;
            }
        }

        // Check for repeated characters (e.g. "aaa", "111")
        if (password.matches(".*(.)\\1{2,}.*")) {
            score -= 10;
            suggestions.add("Avoid repeating the same character 3 or more times consecutively.");
        }

        score = Math.max(0, Math.min(100, score));

        String label;
        if (score < 40) {
            label = "Weak";
        } else if (score < 70) {
            label = "Fair";
        } else if (score < 90) {
            label = "Strong";
        } else {
            label = "Very Strong";
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Password meets all strong security parameters!");
        }

        return PasswordGenerationResponse.builder()
                .password(password)
                .strengthScore(score)
                .strengthLabel(label)
                .suggestions(suggestions)
                .build();
    }
}
