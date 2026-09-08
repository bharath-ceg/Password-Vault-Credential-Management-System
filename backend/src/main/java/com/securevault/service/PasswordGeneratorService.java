package com.securevault.service;

import com.securevault.dto.request.GeneratePasswordRequest;
import com.securevault.dto.response.PasswordGenerationResponse;

public interface PasswordGeneratorService {
    PasswordGenerationResponse generatePassword(GeneratePasswordRequest request);
    PasswordGenerationResponse analyzePassword(String password);
}
