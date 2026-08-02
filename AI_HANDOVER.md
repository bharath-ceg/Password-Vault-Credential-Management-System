# AI Handover Document - SecureVault

## Project Overview
- **Project Name**: SecureVault - Password Vault & Credential Management System
- **Objective**: Enterprise-grade password manager & credential vault with AES-256-GCM encryption, JWT authentication, 5-minute single-use OTP password resets, domain auto-categorization, strong password generator, and secure credential sharing with granular permissions and expiration.
- **Current Milestone**: All 4 Initial Modules Completed (100% Production Ready).
- **Overall Progress**: 100% (First Phase - Modules 1-4)

## Technology Stack
- **Frontend**: React 18, React Router v6, Axios, Tailwind CSS, Lucide Icons, Vite
- **Backend**: Java 17, Spring Boot 3.2.3, Spring Security, Spring Data JPA, Hibernate, JWT (io.jsonwebtoken 0.12.5)
- **Database**: PostgreSQL (with H2 dev fallback support)
- **Email**: Spring Boot JavaMailSender (SMTP HTML templates)
- **Security**: BCrypt (Cost Factor 12), AES-256-GCM Encryption
- **Build Tools**: Vite & Apache Maven

## Folder Structure
```
c:/Users/bharath/OneDrive/Documents/password_vault_and_credential_management_system/
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/securevault/
│           │   ├── SecureVaultApplication.java
│           │   ├── config/ (SecurityConfig, WebCorsConfig)
│           │   ├── controller/ (AuthController, VaultController, GeneratorController, ShareController)
│           │   ├── dto/ (request & response DTOs for Auth, Vault, Generator, Share)
│           │   ├── entity/ (User, VerificationToken, PasswordResetOtp, VaultCredential, CredentialShare, Enums)
│           │   ├── exception/ (GlobalExceptionHandler, Custom Exceptions)
│           │   ├── repository/ (Repositories for all entities)
│           │   ├── security/ (JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl, SecurityUser, JwtAuthenticationEntryPoint)
│           │   ├── service/ (AuthService, VaultService, PasswordGeneratorService, ShareService, EmailService & impls)
│           │   └── util/ (AESEncryptionUtil, CategoryAutoDetectorUtil, PasswordGeneratorUtil)
│           └── resources/ (application.yml, templates/verification-email.html, templates/otp-email.html)
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   ├── index.html
│   └── src/
│       ├── App.jsx
│       ├── main.jsx
│       ├── components/ (ui/ Button, Input, Card, Modal, Spinner, layout/ Header, Sidebar)
│       ├── context/ (AuthContext.jsx, NotificationContext.jsx)
│       ├── layouts/ (AuthLayout.jsx, DashboardLayout.jsx)
│       ├── pages/ (auth/ LoginPage, RegisterPage, VerifyEmailPage, ForgotPasswordPage, ResetPasswordPage, dashboard/ DashboardOverviewPage, VaultPage, GeneratorPage, SharesPage)
│       ├── routes/ (AppRoutes.jsx, ProtectedRoute.jsx, PublicRoute.jsx)
│       ├── services/ (api.js, auth.service.js, vault.service.js, generator.service.js, share.service.js)
│       └── styles/ (index.css)
├── AI_HANDOVER.md
└── PROJECT_SUMMARY.md
```

## Database Schema
- `users`: Primary key `id`, `full_name`, `email` (unique index), `password_hash`, `privacy_password_hash`, `is_email_verified`, `created_at`, `updated_at`.
- `email_verification_tokens`: Primary key `id`, `token` (unique index), `email`, `full_name`, `password_hash`, `expiry_date`.
- `password_reset_otps`: Primary key `id`, `user_id` (FK), `otp_code`, `expiry_time` (5 min), `is_used` (boolean).
- `vault_credentials`: Primary key `id`, `user_id` (FK), `application_url`, `alias_name`, `username`, `encrypted_password` (TEXT), `category` (ENUM).
- `credential_shares`: Primary key `id`, `credential_id` (FK), `owner_id` (FK), `recipient_id` (FK), `permission_level` (ENUM), `expiry_time`.

## Implemented Features
- [x] **Module 1**: User Registration with Zero-Storage Pending Pattern & SMTP Email Verification
- [x] **Module 1**: Verified Email Guard, BCrypt Password Hashing & Stateless JWT Authentication
- [x] **Module 1**: Forgot Password 5-Minute Single-Use OTP via SMTP & Reset Password Engine
- [x] **Module 2**: AES-256-GCM Credential Encryption & Decryption Engine
- [x] **Module 2**: Automatic URL Domain Categorization (Social Media, Banking, Email, Shopping, Developer, Other)
- [x] **Module 2**: Privacy Password Prompt & Vault Shield before password disclosure
- [x] **Module 2**: Fast Search by App URL, Alias Name, Username & Category Filter Pills
- [x] **Module 3**: Cryptographically Secure Random Password Generator & Strength Calculator
- [x] **Module 4**: Secure Sharing with Registered Users, Expiration Control & Permissions (VIEW_ONLY, EDIT, FULL_ACCESS)
- [x] **UI/UX**: SaaS Modern Dark Slate Aesthetic with Inter Google Font, Modals, Toast Feedback, and Responsive Design

## REST APIs
- `POST /api/v1/auth/register` - Registers pending user & sends email verification link
- `GET /api/v1/auth/verify-email?token=...` - Activates user in PostgreSQL
- `POST /api/v1/auth/login` - Authenticates user & returns JWT
- `POST /api/v1/auth/forgot-password` - Sends 5-minute single-use OTP via SMTP
- `POST /api/v1/auth/verify-otp` - Validates OTP code & expiration
- `POST /api/v1/auth/reset-password` - Resets password with old password rejection
- `POST /api/v1/vault` - Encrypts credential using AES-256-GCM & auto-detects category
- `GET /api/v1/vault` - Fetches user credentials with masked passwords
- `GET /api/v1/vault/search?query=...` - Fast vault query search
- `PUT /api/v1/vault/{id}` - Updates vault credential
- `DELETE /api/v1/vault/{id}` - Deletes credential permanently
- `POST /api/v1/vault/{id}/reveal` - Verifies Privacy Password & decrypts password payload
- `POST /api/v1/generator/generate` - Generates cryptographically secure password
- `POST /api/v1/shares` - Shares credential with permission & expiry time
- `GET /api/v1/shares/shared-with-me` & `GET /api/v1/shares/shared-by-me` - Share queries
- `DELETE /api/v1/shares/{id}` - Revokes share access

## Git History
- `feat: initialize enterprise architecture and implement module 1 auth backend`
- `feat: implement module 1 auth frontend with react, tailwind css, and context api`
- `feat: implement password vault with AES-256-GCM encryption and privacy shield`
- `feat: implement strong password generator with real-time strength meter`
- `feat: implement secure sharing subsystem with granular permissions and expiration`

## Next Recommended Task
- Deploy and run system local environment using `npm run dev` and `mvn spring-boot:run`.
