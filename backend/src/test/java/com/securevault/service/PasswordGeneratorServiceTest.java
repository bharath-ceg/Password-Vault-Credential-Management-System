package com.securevault.service;

import com.securevault.dto.request.GeneratePasswordRequest;
import com.securevault.dto.response.PasswordGenerationResponse;
import com.securevault.exception.BadRequestException;
import com.securevault.service.impl.PasswordGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorServiceTest {

    private PasswordGeneratorServiceImpl generatorService;

    @BeforeEach
    void setUp() {
        generatorService = new PasswordGeneratorServiceImpl();
    }

    @Test
    @DisplayName("Generate Password - All Character Sets Enabled")
    void testGeneratePassword_AllSets() {
        GeneratePasswordRequest request = new GeneratePasswordRequest(16, true, true, true, true);

        PasswordGenerationResponse response = generatorService.generatePassword(request);

        assertNotNull(response);
        assertEquals(16, response.getPassword().length());
        assertTrue(response.getPassword().matches(".*[A-Z].*"));
        assertTrue(response.getPassword().matches(".*[a-z].*"));
        assertTrue(response.getPassword().matches(".*[0-9].*"));
        assertTrue(response.getStrengthScore() >= 80);
    }

    @Test
    @DisplayName("Generate Password - No Character Set Selected Throws BadRequestException")
    void testGeneratePassword_NoSetSelected() {
        GeneratePasswordRequest request = new GeneratePasswordRequest(12, false, false, false, false);

        assertThrows(BadRequestException.class, () -> generatorService.generatePassword(request));
    }

    @Test
    @DisplayName("Analyze Password - Weak Password")
    void testAnalyzePassword_Weak() {
        PasswordGenerationResponse response = generatorService.analyzePassword("123456");

        assertEquals("Weak", response.getStrengthLabel());
        assertTrue(response.getStrengthScore() < 40);
        assertFalse(response.getSuggestions().isEmpty());
    }

    @Test
    @DisplayName("Analyze Password - Very Strong Password")
    void testAnalyzePassword_VeryStrong() {
        PasswordGenerationResponse response = generatorService.analyzePassword("K9#vX!2$mL9@pZ4&");

        assertEquals("Very Strong", response.getStrengthLabel());
        assertTrue(response.getStrengthScore() >= 90);
    }
}
