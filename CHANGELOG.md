# SecureVault - Changelog

All changes made to **SecureVault – Password Vault & Credential Management System** during the system refactoring and enterprise redesign.

---

## 1. Added Files

| File Path | Description / Reason |
| :--- | :--- |
| `pom.xml` | Root Maven aggregator POM linking `backend` as a sub-module for automatic IDE & Maven project recognition. |
| `.vscode/settings.json` | VS Code workspace configuration enabling automatic Java Language Server Maven indexing. |
| `password-vault.code-workspace` | Multi-root workspace definition for VS Code managing `backend` and `frontend`. |
| `PROJECT_ANALYSIS.md` | Initial architecture analysis, database schema review, API breakdown, problem audit, and planned modifications. |
| `backend/src/main/java/com/securevault/dto/request/ChangePrivacyPasswordRequest.java` | DTO for multi-step Privacy Password change workflow (Login Password + Current Privacy Password + New Privacy Password validation). |

---

## 2. Deleted Files (Purged Unused Modules)

### Backend Files Removed
- `backend/src/main/java/com/securevault/controller/GeneratorController.java`
- `backend/src/main/java/com/securevault/service/PasswordGeneratorService.java`
- `backend/src/main/java/com/securevault/service/impl/PasswordGeneratorServiceImpl.java`
- `backend/src/main/java/com/securevault/dto/request/GeneratePasswordRequest.java`
- `backend/src/main/java/com/securevault/dto/response/PasswordGeneratorResponse.java`
- `backend/src/main/java/com/securevault/controller/ShareController.java`
- `backend/src/main/java/com/securevault/service/ShareService.java`
- `backend/src/main/java/com/securevault/service/impl/ShareServiceImpl.java`
- `backend/src/main/java/com/securevault/repository/CredentialShareRepository.java`
- `backend/src/main/java/com/securevault/entity/CredentialShare.java`
- `backend/src/main/java/com/securevault/entity/enums/SharePermission.java`
- `backend/src/main/java/com/securevault/dto/request/ShareCredentialRequest.java`
- `backend/src/main/java/com/securevault/dto/response/CredentialShareResponse.java`

### Frontend Files Removed
- `frontend/src/pages/dashboard/GeneratorPage.jsx`
- `frontend/src/pages/dashboard/SharesPage.jsx`
- `frontend/src/services/generator.service.js`
- `frontend/src/services/share.service.js`

*Reason*: Completely removed deprecated Password Generator and Secure Sharing modules to streamline the application, improve security focus, and eliminate dead code.

---

## 3. Modified Files

| File Path | Description of Changes |
| :--- | :--- |
| `backend/src/main/java/com/securevault/dto/request/RegisterRequest.java` | Added `@Pattern` regex enforcing minimum 8 characters, uppercase letter, lowercase letter, and number, with exact error message `"Password must be at least 8 characters long and include uppercase and lowercase letters and a number."`. |
| `backend/src/main/java/com/securevault/dto/request/ResetPasswordRequest.java` | Aligned password validation regex with registration constraints. |
| `backend/src/main/java/com/securevault/dto/request/VaultCredentialRequest.java` | Removed mandatory `@NotBlank` on `applicationUrl` and `aliasName`. Required `username` and `password` remain mandatory. |
| `backend/src/main/java/com/securevault/dto/request/SetPrivacyPasswordRequest.java` | Enforced 8+ character password validation with uppercase, lowercase, number, and special character. |
| `backend/src/main/java/com/securevault/service/VaultService.java` | Declared `hasPrivacyPassword(userEmail)` and `changePrivacyPassword(userEmail, request)` interface methods. |
| `backend/src/main/java/com/securevault/service/impl/VaultServiceImpl.java` | Implemented credential validation rule (at least ONE of URL or Alias must be present; error message `"Please provide either an Application URL or an Alias Name."`), `hasPrivacyPassword`, `changePrivacyPassword`, and exact reveal error message `"Incorrect Privacy Password."`. |
| `backend/src/main/java/com/securevault/controller/VaultController.java` | Added `GET /api/v1/vault/privacy-password/status` and `POST /api/v1/vault/change-privacy-password` endpoints. |
| `backend/src/main/java/com/securevault/config/SecurityConfig.java` | Removed `/api/v1/generator/**` permitAll matcher, updated `frameOptions` to modern lambda syntax, zeroed compiler warnings. |
| `backend/src/main/java/com/securevault/entity/VaultCredential.java` | Made `application_url` and `alias_name` columns nullable in JPA entity mapping. |
| `frontend/tailwind.config.js` | Updated theme color definitions to clean white enterprise palette (`bg-slate-50`, crisp white cards `#ffffff`, primary blue `#2563eb`). |
| `frontend/src/styles/index.css` | Implemented white enterprise theme styling system. |
| `frontend/src/services/vault.service.js` | Added `getPrivacyPasswordStatus` and `changePrivacyPassword` service calls. |
| `frontend/src/routes/AppRoutes.jsx` | Unregistered `/dashboard/generator` and `/dashboard/shares` routes. |
| `frontend/src/layouts/AuthLayout.jsx` | Redesigned auth container with white enterprise branding. |
| `frontend/src/layouts/DashboardLayout.jsx` | Redesigned sidebar and header without generator/share links. |
| `frontend/src/components/ui/Button.jsx` | Redesigned with clean enterprise borders, focus rings, and soft shadows. |
| `frontend/src/components/ui/Input.jsx` | Redesigned with clean light styling and clear realistic placeholders. |
| `frontend/src/components/ui/Card.jsx` | Redesigned with white card container and light slate borders. |
| `frontend/src/components/ui/Modal.jsx` | Added support for `closeable` property for forced first-time setup modal. |
| `frontend/src/pages/auth/RegisterPage.jsx` | Added real-time password requirement checklist, realistic placeholders, and exact backend error message handling. |
| `frontend/src/pages/auth/LoginPage.jsx` | Redesigned with white enterprise theme and realistic placeholders (`Enter your email address`, `Enter your password`). |
| `frontend/src/pages/auth/ForgotPasswordPage.jsx` | Redesigned with white enterprise theme and realistic placeholders. |
| `frontend/src/pages/auth/ResetPasswordPage.jsx` | Redesigned with real-time password requirement checklist and white enterprise styling. |
| `frontend/src/pages/auth/VerifyEmailPage.jsx` | Redesigned with white enterprise status cards. |
| `frontend/src/pages/dashboard/DashboardOverviewPage.jsx` | Redesigned overview dashboard with enterprise stats and direct vault navigation. |
| `frontend/src/pages/dashboard/VaultPage.jsx` | Implemented first-time Privacy Password setup modal, Change Privacy Password modal, credential URL/Alias validation, reveal password modal, and Bitwarden/1Password-inspired white enterprise UI. |
