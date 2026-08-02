# SecureVault – Project Analysis & Refactoring Plan

## 1. Existing Architecture Overview

### Backend Architecture
- **Framework**: Spring Boot 3.2.3 running on Java 17/25.
- **Security**: Spring Security 6 with stateless JWT authentication filter (`JwtAuthenticationFilter`), BCrypt password encoder (strength 12), and custom unauthorized handler (`JwtAuthenticationEntryPoint`).
- **Data Persistence**: Spring Data JPA / Hibernate mapped to PostgreSQL database (with H2 fallback for local dev).
- **Communication & Encryption**:
  - Vault credentials encrypted with AES-256-GCM (`AESEncryptionUtil`).
  - HTML emails sent via Spring Mail / SMTP (`EmailServiceImpl`).
- **DTO Layer**: Explicit requests/responses (`RegisterRequest`, `LoginRequest`, `VaultCredentialRequest`, etc.) decoupling JPA Entities from controllers.

### Frontend Architecture
- **Framework**: React 18 with Vite.
- **Routing**: `react-router-dom` v6 with `ProtectedRoute` (requires JWT token) and `PublicRoute` wrappers.
- **HTTP Client**: Axios instance (`api.js`) with request interceptors attaching `Authorization: Bearer <token>` header and response interceptors catching 401s.
- **State Management**: `AuthContext.jsx` handling user state, token persistence in `localStorage`, login, and logout.
- **UI Components**: Reusable modular components (`Button`, `Input`, `Card`, `Modal`, `Spinner`).

---

## 2. Existing Database Schema

### `users` Table
- `id` (BIGINT, PK, Auto-increment)
- `full_name` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(150), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL) - BCrypt hash of account login password
- `privacy_password_hash` (VARCHAR(255), NULLABLE) - BCrypt hash of secondary privacy password
- `is_email_verified` (BOOLEAN, DEFAULT FALSE)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### `vault_credentials` Table
- `id` (BIGINT, PK, Auto-increment)
- `user_id` (BIGINT, FK -> `users.id`, NOT NULL)
- `application_url` (VARCHAR(500), NULLABLE)
- `alias_name` (VARCHAR(150), NULLABLE)
- `username` (VARCHAR(150), NOT NULL)
- `encrypted_password` (TEXT, NOT NULL) - AES-256-GCM encrypted
- `category` (VARCHAR(50), NOT NULL)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### `password_reset_otps` Table
- `id` (BIGINT, PK, Auto-increment)
- `email` (VARCHAR(150), NOT NULL)
- `otp` (VARCHAR(10), NOT NULL)
- `expiry_time` (TIMESTAMP, NOT NULL)
- `is_used` (BOOLEAN, DEFAULT FALSE)
- `created_at` (TIMESTAMP)

### `verification_tokens` Table
- `id` (BIGINT, PK, Auto-increment)
- `token` (VARCHAR(255), UNIQUE, NOT NULL)
- `email` (VARCHAR(150), NOT NULL)
- `full_name` (VARCHAR(100), NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL)
- `expiry_time` (TIMESTAMP, NOT NULL)
- `created_at` (TIMESTAMP)

### `credential_shares` Table *(TO BE REMOVED)*
- Deprecated sharing relationship table.

---

## 3. Existing APIs

| Endpoint | Method | Access | Description |
| :--- | :--- | :--- | :--- |
| `/api/v1/auth/register` | POST | Public | Registers pending user & dispatches verification email |
| `/api/v1/auth/verify-email` | GET | Public | Validates token & creates verified user |
| `/api/v1/auth/login` | POST | Public | Validates credentials & returns JWT |
| `/api/v1/auth/forgot-password` | POST | Public | Dispatches 5-minute OTP for password reset |
| `/api/v1/auth/verify-otp` | POST | Public | Validates OTP token |
| `/api/v1/auth/reset-password` | POST | Public | Resets password with valid OTP |
| `/api/v1/auth/me` | GET | Authenticated | Fetches current user profile |
| `/api/v1/vault` | GET | Authenticated | Fetches user's vault credentials |
| `/api/v1/vault` | POST | Authenticated | Adds new vault credential |
| `/api/v1/vault/{id}` | PUT | Authenticated | Updates vault credential |
| `/api/v1/vault/{id}` | DELETE | Authenticated | Deletes vault credential |
| `/api/v1/vault/{id}/reveal` | POST | Authenticated | Verifies Privacy Password & returns decrypted password |
| `/api/v1/vault/privacy-password` | POST | Authenticated | Sets up or updates Privacy Password |
| `/api/v1/vault/change-privacy-password` | POST | Authenticated | Changes Privacy Password (with login & current privacy verification) |
| `/api/v1/generator/**` | ALL | *TO REMOVE* | Password generator endpoints |
| `/api/v1/shares/**` | ALL | *TO REMOVE* | Secure sharing endpoints |

---

## 4. Existing Problems Found

1. **Weak Password Validation**:
   - `RegisterRequest.java` and `ResetPasswordRequest.java` used `@Size(min = 6)` which permitted purely numeric passwords (e.g. `12345678`).
   - Generic error messages like "Validation Failed" were returned instead of specific feedback.

2. **Strict Credential Validation Requirements**:
   - `VaultCredentialRequest.java` mandated `@NotBlank` for both `applicationUrl` and `aliasName`.
   - The user requirement specifies that `username` and `password` are mandatory, while `applicationUrl` and `aliasName` are optional—provided that **at least one** of `applicationUrl` or `aliasName` is present.

3. **Incomplete Privacy Password Workflow**:
   - No explicit API or front-end prompt for first-time Privacy Password set-up when visiting the Vault.
   - Missing dedicated "Change Privacy Password" multi-step verification flow.

4. **Unused Modules & Bloat**:
   - Password Generator and Secure Sharing modules remain in the codebase, creating maintenance overhead and unnecessary complexity.

5. **Compiler & IDE Warnings**:
   - Spring Security configuration deprecation warnings in `SecurityConfig.java`.
   - Unused imports and unused endpoints across multiple backend classes.

6. **Aesthetics & UI Styling**:
   - Dark glassmorphism and generic AI gradients look out of place for an enterprise-grade credential management tool.
   - Requires a clean, modern white enterprise layout inspired by Bitwarden, 1Password, and GitHub.

---

## 5. Files to be Modified & Added

### Backend Files to Modify
- `RegisterRequest.java`: Add regex validation (`^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$`) with custom message: *"Password must be at least 8 characters long and include uppercase and lowercase letters and a number."*
- `ResetPasswordRequest.java`: Align password constraints with registration rules.
- `VaultCredentialRequest.java`: Remove mandatory `@NotBlank` on URL and Alias; add custom class-level validation logic.
- `VaultServiceImpl.java`: Update validation for credential creation/update, add `changePrivacyPassword` service method, update Privacy Password logic.
- `VaultController.java`: Add `hasPrivacyPassword` endpoint and `changePrivacyPassword` endpoint.
- `SecurityConfig.java`: Remove deprecated generator permitAll endpoints, update headers syntax (`frameOptions`), fix all warnings.
- `User.java`: Ensure `@Column` annotations for `privacy_password_hash` and auditing timestamps are fully aligned with PostgreSQL.
- `DataInitializer.java`: Update sample data seeding to align with updated validation rules.

### Backend Files to Delete (Unused Modules)
- `GeneratorController.java`
- `PasswordGeneratorService.java`, `PasswordGeneratorServiceImpl.java`
- `GeneratePasswordRequest.java`, `PasswordGeneratorResponse.java`
- `ShareController.java`, `ShareService.java`, `ShareServiceImpl.java`
- `CredentialShareRepository.java`, `CredentialShare.java`, `SharePermission.java`
- `ShareCredentialRequest.java`, `CredentialShareResponse.java`

### Frontend Files to Modify
- `index.css`: Complete redesign with white enterprise styling, Bitwarden/GitHub palette, custom clean utility classes.
- `RegisterPage.jsx`: Real-time password validation indicators and user-friendly messages.
- `VaultPage.jsx`: Enterprise card layout, first-time Privacy Password setup modal, Reveal modal, Change Privacy Password modal, updated credential form with optional URL/Alias indicators.
- `DashboardLayout.jsx`: Clean, professional sidebar & header without generator/share links.
- `DashboardOverviewPage.jsx`: Redesigned quick metrics and recent credentials view.
- `AppRoutes.jsx`: Remove routes for generator and shares.
- `vault.service.js`: Add API calls for privacy password status check and password change.
- `auth.service.js`: Update validation handling.

### Frontend Files to Delete
- `GeneratorPage.jsx`
- `SharesPage.jsx`
- `generator.service.js`
- `share.service.js`
