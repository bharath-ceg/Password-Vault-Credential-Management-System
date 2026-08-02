package com.securevault.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PasswordGeneratorUtil {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom random = new SecureRandom();

    public String generatePassword(int length, boolean useUpper, boolean useLower, boolean useNumbers, boolean useSpecial) {
        if (length < 4) length = 4;
        if (length > 128) length = 128;

        StringBuilder charPool = new StringBuilder();
        List<Character> passwordChars = new ArrayList<>();

        if (useUpper) {
            charPool.append(UPPERCASE);
            passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (useLower) {
            charPool.append(LOWERCASE);
            passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (useNumbers) {
            charPool.append(NUMBERS);
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (useSpecial) {
            charPool.append(SPECIAL);
            passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        if (charPool.length() == 0) {
            charPool.append(LOWERCASE).append(NUMBERS);
            passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }

        int remainingLength = length - passwordChars.size();
        for (int i = 0; i < remainingLength; i++) {
            passwordChars.add(charPool.charAt(random.nextInt(charPool.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder result = new StringBuilder();
        for (char c : passwordChars) {
            result.append(c);
        }

        return result.toString();
    }

    public int calculateStrengthScore(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 20;
        if (password.length() >= 16) score += 10;

        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score += 10;

        return Math.min(score, 100);
    }
}
