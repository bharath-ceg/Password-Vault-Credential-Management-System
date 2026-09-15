# SecureVault – Deployment Guide (Vercel + Render Cloud & Docker Local)

This document provides complete, step-by-step instructions for deploying the **SecureVault Password Vault & Credential Management System** to production using **Vercel** (Frontend SPA), **Render** (Spring Boot Docker Web Service), and **Render PostgreSQL** (Database), as well as local containerized deployment via Docker Compose.

---

## 1. System Architecture

```text
                INTERNET
                    │
                    ▼
             VERCEL FRONTEND
             React 18 + Vite SPA
                    │
                    ▼ (HTTPS REST API /api/v1/*)
          RENDER SPRING BOOT API
           Docker Container (Java 17)
                    │
                    ▼ (Internal / Encrypted JDBC)
            RENDER POSTGRESQL
            Managed Database (PostgreSQL 15)
```

- **Frontend**: React 18 SPA built with Vite, deployed natively on **Vercel**.
- **Backend**: Spring Boot 3.2 REST API packaged via **Docker** and deployed as a Web Service on **Render**.
- **Database**: Managed **Render PostgreSQL** database storing encrypted credentials, users, audit logs, and security alerts.
- **Local Deployment**: Full stack containerization using **Docker Compose** (`docker-compose.yml`) for local development and testing.

---

## 2. Environment Variables Specification

Environment configuration is split by platform to maintain maximum security and separation of concerns.

### 2.1 Vercel Environment Variables (Frontend)

| Variable | Description | Example / Production Value |
|---|---|---|
| `VITE_API_URL` | Base endpoint URL of your deployed Render Spring Boot API | `https://securevault-backend.onrender.com/api/v1` |

### 2.2 Render Environment Variables (Backend Web Service)

| Variable | Description | Example / Production Value |
|---|---|---|
| `PORT` | Container web server port (automatically populated by Render) | `8080` (or Render assigned port) |
| `DATABASE_URL` | JDBC connection string to Render PostgreSQL | `jdbc:postgresql://dpg-xxxx-a:5432/securevault_db` |
| `DATABASE_USERNAME` | Master database user | `postgres` (or Render generated user) |
| `DATABASE_PASSWORD` | Master database password | `[Render DB Password]` |
| `JWT_SECRET` | 256-bit secret key for signing user auth tokens | `[64-character random hex string]` |
| `AES_SECRET_KEY` | 32-character key for credential payload encryption | `[32-character secret key]` |
| `SMTP_HOST` | Email SMTP server host | `smtp.gmail.com` |
| `SMTP_PORT` | Email SMTP server port | `587` |
| `SMTP_USERNAME` | Email account for OTP/security notifications | `your-email@gmail.com` |
| `SMTP_PASSWORD` | App password for Gmail SMTP authentication | `[Gmail App Password]` |
| `VERIFICATION_BASE_URL` | Base URL for user email verification links | `https://securevault-frontend.vercel.app/verify-email` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins | `https://securevault-frontend.vercel.app` |
| `FAILED_LOGIN_THRESHOLD` | Max failed logins allowed before alert | `3` |
| `FAILED_LOGIN_TIME_WINDOW_MINUTES` | Window to measure failed login attempts | `15` |
| `PASSWORD_EXPIRATION_DAYS` | Vault master password health alert threshold | `30` |

### 2.3 Local Development Variables (`.env`)

For local Docker Compose deployment, copy `.env.example` to `.env` and configure your local credentials.

---

## 3. Render Deployment Steps (Backend & Database)

### Step 3.1: Create Render PostgreSQL Database

1. Log into your [Render Dashboard](https://dashboard.render.com).
2. Click **New +** -> **PostgreSQL**.
3. Configure the database details:
   - **Name**: `securevault-postgres`
   - **Database**: `securevault_db`
   - **User**: `postgres`
   - **Region**: Select your preferred region (e.g., Singapore or Oregon)
   - **Plan**: Free / Standard
4. Click **Create Database**.
5. Once created, copy the following values from the database dashboard:
   - **Internal Database URL** (e.g., `postgresql://postgres:password@dpg-xxx-a:5432/securevault_db`)
   - **Internal Hostname** (e.g., `dpg-xxx-a`)
   - **Database Name**, **Username**, and **Password**

> ℹ️ **JDBC Note**: Spring Boot requires the JDBC format (`jdbc:postgresql://dpg-xxx-a:5432/securevault_db`). Replace `postgresql://` with `jdbc:postgresql://` when configuring `DATABASE_URL`.

---

### Step 3.2: Create Render Spring Boot Web Service (Docker)

Option A: **Manual Render Web Service Creation**
1. In Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository (`Password-Vault-Credential-Management-System`).
3. Select **Docker** as the Runtime environment.
4. Set **Root Directory**: `backend`
5. Set **Dockerfile Path**: `Dockerfile` (or `./Dockerfile` relative to backend root)
6. Under **Environment Variables**, add the required backend variables:
   - `PORT` = `8080`
   - `DATABASE_URL` = `jdbc:postgresql://[Internal Hostname]:5432/securevault_db`
   - `DATABASE_USERNAME` = `postgres` (or your Render DB username)
   - `DATABASE_PASSWORD` = `[Your Render DB Password]`
   - `JWT_SECRET` = `[Your 64-char Hex Secret]`
   - `AES_SECRET_KEY` = `[Your 32-char Secret]`
   - `SMTP_HOST` = `smtp.gmail.com`
   - `SMTP_PORT` = `587`
   - `SMTP_USERNAME` = `[Your Email]`
   - `SMTP_PASSWORD` = `[Your Gmail App Password]`
   - `VERIFICATION_BASE_URL` = `https://[YOUR-VERCEL-APP].vercel.app/verify-email`
   - `CORS_ALLOWED_ORIGINS` = `https://[YOUR-VERCEL-APP].vercel.app`
7. Click **Create Web Service**.

Option B: **Render Blueprint (`render.yaml`)**
1. Click **New +** -> **Blueprint**.
2. Connect the repository. Render will automatically detect `render.yaml` at the root and prompt for missing sensitive variables (`SMTP_USERNAME`, `SMTP_PASSWORD`, `VERIFICATION_BASE_URL`, `CORS_ALLOWED_ORIGINS`).
3. Click **Apply**.

---

### Step 3.3: Obtain Render Backend Base URL

Once deployment completes and logs indicate `Started SecureVaultApplication`:
1. Copy your Web Service URL from the top of the Render dashboard (e.g., `https://securevault-backend.onrender.com`).
2. Test backend health: Open `https://securevault-backend.onrender.com/api/v1/auth/me` in a browser. It should return a `401 Unauthorized` JSON response, confirming the Spring Boot API is active.

---

## 4. Vercel Deployment Steps (Frontend SPA)

### Step 4.1: Connect Repository to Vercel

1. Log into your [Vercel Dashboard](https://vercel.com/dashboard).
2. Click **Add New...** -> **Project**.
3. Import your GitHub repository (`Password-Vault-Credential-Management-System`).

### Step 4.2: Configure Project Build Settings

1. Set **Framework Preset**: `Vite`
2. Set **Root Directory**: `frontend`
3. Expand **Build and Output Settings**:
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`

### Step 4.3: Configure Environment Variables

Under **Environment Variables**, add:
- `VITE_API_URL` = `https://securevault-backend.onrender.com/api/v1` (Replace with your actual Render backend URL)

### Step 4.4: Deploy & Verify SPA Routes

1. Click **Deploy**.
2. After deployment finishes, open your assigned Vercel URL (e.g., `https://securevault-frontend.vercel.app`).
3. Test direct route refreshes (e.g., navigate directly to `/login` or `/register` and reload the page). `frontend/vercel.json` guarantees React Router paths resolve without 404 errors.

---

## 5. Local Docker Deployment Instructions

For local containerized deployment, run the full stack using Docker Compose:

### Step 1: Copy Environment Template
```bash
cp .env.example .env
```
*(Optionally edit `.env` to customize local secret keys or email credentials)*

### Step 2: Build & Start Containers
```bash
docker compose build
docker compose up -d
```

### Step 3: Verify Running Services
```bash
docker compose ps
```

Expected Output:
```text
NAME                   IMAGE                               COMMAND                  SERVICE      STATUS                    PORTS
securevault-backend    securevault-and-management-backend  "java -jar app.jar"      backend      Up                        0.0.0.0:8080->8080/tcp
securevault-frontend   securevault-and-management-frontend "nginx -g 'daemon of…"   frontend     Up                        0.0.0.0:80->80/tcp
securevault-postgres   postgres:15-alpine                  "docker-entrypoint.s…"   postgres     Up (healthy)              5432/tcp
```

### Step 4: Access Application Locally
- **Frontend SPA**: [http://localhost](http://localhost)
- **Backend API**: [http://localhost:8080/api/v1](http://localhost:8080/api/v1)

---

## 6. Operational & Redeployment Management

### Redeploying Updates
- **Frontend**: Any push to the `main` branch connected to Vercel triggers an automated build and instant deployment.
- **Backend**: Any push to `main` connected to Render triggers a container rebuild and zero-downtime deployment.

### Monitoring & Logs
- **Render Backend**: View real-time Spring Boot logs under the **Logs** tab of your Render Web Service.
- **Vercel Frontend**: View build & runtime console logs under the **Logs** tab in Vercel.
- **Local Docker**: View logs via `docker compose logs -f [service_name]`.
