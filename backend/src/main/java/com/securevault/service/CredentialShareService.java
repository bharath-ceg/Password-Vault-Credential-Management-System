package com.securevault.service;

import com.securevault.dto.request.CreateShareRequest;
import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.UpdateShareRequest;
import com.securevault.dto.response.CredentialShareResponse;

import java.util.List;

public interface CredentialShareService {
    CredentialShareResponse createShare(String ownerEmail, CreateShareRequest request);
    List<CredentialShareResponse> getMyShares(String ownerEmail);
    List<CredentialShareResponse> getSharedWithMe(String recipientEmail);
    CredentialShareResponse updateShare(String userEmail, Long shareId, UpdateShareRequest request);
    void revokeShare(String userEmail, Long shareId);
    String revealSharedPassword(String recipientEmail, Long shareId, RevealPasswordRequest request);
}
