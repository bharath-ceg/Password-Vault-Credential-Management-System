package com.securevault.controller;

import com.securevault.dto.request.CreateShareRequest;
import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.UpdateShareRequest;
import com.securevault.dto.response.ApiResponse;
import com.securevault.dto.response.CredentialShareResponse;
import com.securevault.service.CredentialShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shares")
public class CredentialShareController {

    private final CredentialShareService shareService;

    public CredentialShareController(CredentialShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CredentialShareResponse>> createShare(
            Authentication authentication,
            @Valid @RequestBody CreateShareRequest request) {
        CredentialShareResponse share = shareService.createShare(authentication.getName(), request);
        return new ResponseEntity<>(ApiResponse.success("Credential shared successfully", share), HttpStatus.CREATED);
    }

    @GetMapping("/my-shares")
    public ResponseEntity<ApiResponse<List<CredentialShareResponse>>> getMyShares(Authentication authentication) {
        List<CredentialShareResponse> shares = shareService.getMyShares(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("My shared credentials retrieved successfully", shares));
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<ApiResponse<List<CredentialShareResponse>>> getSharedWithMe(Authentication authentication) {
        List<CredentialShareResponse> shares = shareService.getSharedWithMe(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Credentials shared with me retrieved successfully", shares));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CredentialShareResponse>> updateShare(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateShareRequest request) {
        CredentialShareResponse updated = shareService.updateShare(authentication.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Credential share updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> revokeShare(
            Authentication authentication,
            @PathVariable("id") Long id) {
        shareService.revokeShare(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Credential share revoked successfully"));
    }

    @PostMapping("/{id}/reveal")
    public ResponseEntity<ApiResponse<String>> revealSharedPassword(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody RevealPasswordRequest request) {
        String decryptedPassword = shareService.revealSharedPassword(authentication.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Shared password decrypted successfully", decryptedPassword));
    }
}
