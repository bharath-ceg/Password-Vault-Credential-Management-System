package com.securevault.controller;

import com.securevault.dto.request.ChangePrivacyPasswordRequest;
import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.SetPrivacyPasswordRequest;
import com.securevault.dto.request.VaultCredentialRequest;
import com.securevault.dto.response.ApiResponse;
import com.securevault.dto.response.VaultCredentialResponse;
import com.securevault.entity.enums.CredentialCategory;
import com.securevault.service.VaultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VaultCredentialResponse>> createCredential(
            Authentication authentication,
            @Valid @RequestBody VaultCredentialRequest request) {
        VaultCredentialResponse credential = vaultService.createCredential(authentication.getName(), request);
        return new ResponseEntity<>(ApiResponse.success("Credential created & encrypted successfully", credential), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VaultCredentialResponse>>> getCredentials(
            Authentication authentication,
            @RequestParam(value = "category", required = false) CredentialCategory category) {
        List<VaultCredentialResponse> credentials = vaultService.getUserCredentials(authentication.getName(), category);
        return ResponseEntity.ok(ApiResponse.success("Credentials fetched successfully", credentials));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VaultCredentialResponse>>> searchCredentials(
            Authentication authentication,
            @RequestParam("query") String query) {
        List<VaultCredentialResponse> results = vaultService.searchCredentials(authentication.getName(), query);
        return ResponseEntity.ok(ApiResponse.success("Search completed", results));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultCredentialResponse>> updateCredential(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody VaultCredentialRequest request) {
        VaultCredentialResponse updated = vaultService.updateCredential(authentication.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Credential updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCredential(
            Authentication authentication,
            @PathVariable("id") Long id) {
        vaultService.deleteCredential(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Credential deleted permanently"));
    }

    @PostMapping("/{id}/reveal")
    public ResponseEntity<ApiResponse<String>> revealPassword(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody RevealPasswordRequest request) {
        String decryptedPassword = vaultService.revealPassword(authentication.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Password decrypted successfully", decryptedPassword));
    }

    @GetMapping("/privacy-password/status")
    public ResponseEntity<ApiResponse<Boolean>> getPrivacyPasswordStatus(Authentication authentication) {
        boolean hasPassword = vaultService.hasPrivacyPassword(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Privacy password status fetched", hasPassword));
    }

    @PostMapping("/privacy-password")
    public ResponseEntity<ApiResponse<String>> setPrivacyPassword(
            Authentication authentication,
            @Valid @RequestBody SetPrivacyPasswordRequest request) {
        vaultService.setPrivacyPassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Privacy Password configured successfully."));
    }

    @PostMapping("/change-privacy-password")
    public ResponseEntity<ApiResponse<String>> changePrivacyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePrivacyPasswordRequest request) {
        vaultService.changePrivacyPassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Privacy password updated successfully."));
    }
}
