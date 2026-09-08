package com.securevault.controller;

import com.securevault.dto.response.SecurityAnalyticsResponse;
import com.securevault.service.SecurityMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SecurityMonitoringService securityMonitoringService;

    @InjectMocks
    private SecurityController securityController;

    private final String testEmail = "analyticsuser@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(securityController).build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testEmail, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("GET /api/v1/security/analytics - Success")
    void testGetSecurityAnalytics_Success() throws Exception {
        SecurityAnalyticsResponse analytics = SecurityAnalyticsResponse.builder()
                .totalLoginAttempts(30)
                .successfulLogins(25)
                .failedLogins(5)
                .suspiciousActivities(2)
                .securityAlerts(1)
                .recentActivities(Collections.emptyList())
                .loginLogs(Collections.emptyList())
                .suspiciousActivityLogs(Collections.emptyList())
                .securityAlertLogs(Collections.emptyList())
                .build();

        when(securityMonitoringService.getSecurityAnalytics(testEmail)).thenReturn(analytics);

        mockMvc.perform(get("/api/v1/security/analytics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalLoginAttempts").value(30))
                .andExpect(jsonPath("$.data.successfulLogins").value(25))
                .andExpect(jsonPath("$.data.failedLogins").value(5))
                .andExpect(jsonPath("$.data.suspiciousActivities").value(2))
                .andExpect(jsonPath("$.data.securityAlerts").value(1));
    }
}
