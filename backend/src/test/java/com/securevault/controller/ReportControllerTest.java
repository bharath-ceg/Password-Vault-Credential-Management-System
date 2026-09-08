package com.securevault.controller;

import com.securevault.dto.response.LoginActivityReportResponse;
import com.securevault.dto.response.PasswordHealthReportResponse;
import com.securevault.service.ReportService;
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

class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private final String testEmail = "controlleruser@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testEmail, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("GET /api/v1/reports/password-health - Success")
    void testGetPasswordHealthReport_Success() throws Exception {
        PasswordHealthReportResponse report = PasswordHealthReportResponse.builder()
                .totalCredentials(10)
                .strongPasswords(6)
                .mediumPasswords(3)
                .weakPasswords(1)
                .healthScore(85)
                .build();

        when(reportService.getPasswordHealthReport(testEmail)).thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/password-health")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCredentials").value(10))
                .andExpect(jsonPath("$.data.strongPasswords").value(6))
                .andExpect(jsonPath("$.data.mediumPasswords").value(3))
                .andExpect(jsonPath("$.data.weakPasswords").value(1))
                .andExpect(jsonPath("$.data.healthScore").value(85));
    }

    @Test
    @DisplayName("GET /api/v1/reports/login-activity - Success")
    void testGetLoginActivityReport_Success() throws Exception {
        LoginActivityReportResponse report = LoginActivityReportResponse.builder()
                .totalAttempts(25)
                .successfulLogins(20)
                .failedLogins(5)
                .recentActivities(Collections.emptyList())
                .build();

        when(reportService.getLoginActivityReport(testEmail)).thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/login-activity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAttempts").value(25))
                .andExpect(jsonPath("$.data.successfulLogins").value(20))
                .andExpect(jsonPath("$.data.failedLogins").value(5));
    }
}
