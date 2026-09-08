package com.securevault.service;

import com.securevault.dto.response.LoginActivityReportResponse;
import com.securevault.dto.response.PasswordHealthReportResponse;

public interface ReportService {

    PasswordHealthReportResponse getPasswordHealthReport(String userEmail);

    LoginActivityReportResponse getLoginActivityReport(String userEmail);
}
