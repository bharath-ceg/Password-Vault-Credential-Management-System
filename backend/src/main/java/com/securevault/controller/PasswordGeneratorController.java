package com.securevault.controller;

import com.securevault.dto.request.GeneratePasswordRequest;
import com.securevault.dto.response.ApiResponse;
import com.securevault.dto.response.PasswordGenerationResponse;
import com.securevault.service.PasswordGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/generator")
public class PasswordGeneratorController {

    private final PasswordGeneratorService passwordGeneratorService;

    public PasswordGeneratorController(PasswordGeneratorService passwordGeneratorService) {
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PasswordGenerationResponse>> generatePassword(
            @Valid @RequestBody GeneratePasswordRequest request) {
        PasswordGenerationResponse response = passwordGeneratorService.generatePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password generated successfully", response));
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<PasswordGenerationResponse>> analyzePassword(
            @RequestBody Map<String, String> body) {
        String password = body.getOrDefault("password", "");
        PasswordGenerationResponse response = passwordGeneratorService.analyzePassword(password);
        return ResponseEntity.ok(ApiResponse.success("Password strength analyzed successfully", response));
    }
}
