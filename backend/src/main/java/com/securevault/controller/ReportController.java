package com.securevault.controller;

import com.securevault.dto.response.ApiResponse;
import com.securevault.dto.response.LoginActivityReportResponse;
import com.securevault.dto.response.PasswordHealthReportResponse;
import com.securevault.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/password-health")
    public ResponseEntity<ApiResponse<PasswordHealthReportResponse>> getPasswordHealthReport(Authentication authentication) {
        PasswordHealthReportResponse report = reportService.getPasswordHealthReport(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Password health report generated successfully", report));
    }

    @GetMapping("/login-activity")
    public ResponseEntity<ApiResponse<LoginActivityReportResponse>> getLoginActivityReport(Authentication authentication) {
        LoginActivityReportResponse report = reportService.getLoginActivityReport(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Login activity report generated successfully", report));
    }
}
