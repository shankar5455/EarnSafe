# EarnSafe — AI-Powered Parametric Insurance for India's Gig Delivery Workers

> Guidewire DEVTrails 2026 · Full-Stack Implementation

EarnSafe is a full-stack web platform that protects gig delivery workers (Swiggy, Zomato, Zepto, Blinkit, Amazon, etc.) from **income loss** caused by external disruptions: heavy rain, floods, heatwaves, pollution spikes, and zone closures. The frontend communicates with the backend entirely through REST APIs backed by a live MySQL database.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Problem Statement](#2-problem-statement)
3. [Key Features](#3-key-features)
4. [Tech Stack](#4-tech-stack)
5. [System Architecture](#5-system-architecture)
6. [Folder Structure](#6-folder-structure)
7. [Real-World Workflow](#7-real-world-workflow)
8. [Frontend–Backend Integration](#8-frontendbackend-integration)
9. [Environment Variables & API Keys](#9-environment-variables--api-keys)
10. [Database Setup](#10-database-setup)
11. [Backend Setup](#11-backend-setup)
12. [Frontend Setup](#12-frontend-setup)
13. [Running the Full Project](#13-running-the-full-project)
14. [API Endpoints](#14-api-endpoints)
15. [Authentication Flow](#15-authentication-flow)
16. [Data Flow](#16-data-flow)
17. [Seeder / Demo Data](#17-seeder--demo-data)
18. [Remove Mock Data Checklist](#18-remove-mock-data-checklist)
19. [What API Keys Are Required](#19-what-api-keys-are-required)
20. [Dynamic Premium Engine](#20-dynamic-premium-engine)
21. [Covered Cities & Zones](#21-covered-cities--zones)
22. [Suggested Production Improvements](#22-suggested-production-improvements)
23. [Hackathon Relevance — DEVTrails Mapping](#23-hackathon-relevance--devtrails-mapping)
24. [Future Scope](#24-future-scope)
25. [Recommended Next Files to Update](#25-recommended-next-files-to-update)

---

## 1. Project Overview

EarnSafe is a **parametric micro-insurance platform** built for India's gig economy. Workers subscribe to a weekly income-protection plan. When a verified disruption event (rain > 30 mm, AQI > 300, etc.) occurs in their zone, the system automatically generates a claim — no paperwork, no manual filing. Premiums are calculated dynamically using a risk engine that weighs weather data, zone safety, shift type, and daily earnings.

---

## 2. Problem Statement

India has 15+ million gig delivery workers. When heavy rain hits a city, deliveries stop — but traditional insurance does not cover this kind of income loss. EarnSafe addresses this with:

- **Parametric triggers**: Claims fire automatically when verified thresholds are crossed.
- **Weekly micro-premiums**: Starting at ₹39/week, adjusted per worker risk profile.
- **Income-focused coverage**: Delivery disruption only — not health or vehicle damage.

---

## 3. Key Features

### For Workers
- Register with delivery platform and zone details
- View personalized weekly premium with itemized breakdown
- Activate, pause, renew, or deactivate income-protection policy
- Automatic claim generation on disruption events
- Track claim status and payout history
- View live disruption events affecting their zone

### For Admins
- Platform-wide dashboard with live KPIs (fetched from DB)
- Manage workers, policies, and claims
- Fire trigger events via API (for testing; labeled as test utility)
- Review risk zone data
- Approve, reject, or mark claims as paid

---

## 4. Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + Vite, Tailwind CSS v4, Axios, React Router DOM, Recharts, Lucide React |
| Backend | Java 17, Spring Boot 3.2, Spring Security (JWT), Spring Data JPA |
| Database | MySQL 8.0 |
| Build tools | Maven 3.8+, npm |

---

## 5. System Architecture

```
[Browser / React Frontend]
        │
        │  HTTPS REST (Axios + JWT Bearer token)
        ▼
[Spring Boot Backend  — port 8080]
        │
        ├── Spring Security (JWT filter)
        ├── REST Controllers  →  Service layer  →  JPA Repositories
        └── MySQL Database (earnsafe_db)
```

The frontend never talks to the database directly. All state mutations and reads go through the backend REST API. The backend validates the JWT on every protected request before executing business logic.

---

## 6. Folder Structure

```
EarnSafe/
├── frontend/                        # React + Vite frontend
│   ├── src/
│   │   ├── api/
│   │   │   ├── axios.js             # Centralized Axios client + interceptors
│   │   │   └── index.js             # Named API call functions
│   │   ├── components/              # Reusable UI components
│   │   ├── context/
│   │   │   └── AuthContext.jsx      # JWT storage & global auth state
│   │   ├── pages/                   # Page-level components
│   │   └── App.jsx                  # Route definitions
│   ├── .env                         # Local env (not committed)
│   ├── .env.example                 # Committed template
│   └── package.json
│
├── backend/                         # Spring Boot backend
│   ├── src/main/java/com/earnsafe/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── DataSeeder.java      # Optional startup seeder (disable for production)
│   │   ├── controller/              # REST API endpoints
│   │   ├── dto/                     # Request/Response data transfer objects
│   │   ├── entity/                  # JPA entities (User, Policy, Claim, etc.)
│   │   ├── exception/               # Global exception handler
│   │   ├── repository/              # Spring Data JPA repositories
│   │   ├── security/                # JWT filter and utility
│   │   └── service/                 # Business logic
│   ├── src/main/resources/
│   │   └── application.properties   # All configuration (reads env vars)
│   ├── .env                         # Local env (not committed)
│   ├── .env.example                 # Committed template
│   └── pom.xml
│
└── README.md
```

---

## 7. Real-World Workflow

1. Worker visits the frontend and registers an account.
2. The registration form submits to `POST /api/auth/register`; the backend stores the user in MySQL.
3. Worker logs in via `POST /api/auth/login`; the backend issues a signed JWT.
4. The frontend stores the token in `localStorage` and attaches it as a `Bearer` header on all subsequent requests.
5. Worker requests a premium quote (`POST /api/premium/calculate`); the backend runs the risk engine and returns a breakdown.
6. Worker creates a policy (`POST /api/policies/create`); it is persisted in MySQL.
7. An admin (or the test trigger API) fires a disruption event; the backend evaluates which policies are affected and auto-generates claims.
8. Admin reviews, approves, and marks claims as paid via the admin dashboard, which is entirely API-driven.
9. Worker refreshes the dashboard; data is fetched live from the DB — no cached or static values.

---

## 8. Frontend–Backend Integration

### Axios Client

The centralized Axios client lives at `frontend/src/api/axios.js`. It is already implemented:

```js
// frontend/src/api/axios.js
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Redirect to login on 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Named API Functions

All backend calls are collected in `frontend/src/api/index.js`:

```js
import api from './axios';

export const authApi   = { register, login, me };
export const policyApi = { create, getMy, getById, renew, pause, deactivate };
export const premiumApi = { calculate, explain };
export const claimsApi  = { getMy, getById, approve, reject, markPaid };
export const triggerApi = { getLive, createMockEvent, evaluateAll };
export const adminApi   = { getDashboard, getUsers, getPolicies, getClaims, getRiskZones };
```

### Making Authenticated Requests

Because the request interceptor is in place, any page component can call APIs without manually adding headers:

```js
import { policyApi } from '../api';

const { data } = await policyApi.getMy();
// → GET /api/policies/my  with Authorization: Bearer <token>
```

### Rules for All Pages

- Dashboard cards, policy tables, claim tables, admin stats, trigger event lists, and worker details **must be loaded from backend APIs**.
- No page should contain hardcoded arrays or static JSON as its primary data source.
- Charts must use values returned from backend summary/statistics endpoints.

---

## 9. Environment Variables & API Keys

### Quick Setup

1. Copy `.env.example` from the project root and create a `.env` file (or export the variables in your shell):

```bash
cp .env.example .env
# Then edit .env and fill in DB_PASSWORD and JWT_SECRET
```

2. Run the backend with the environment variables set. Using your shell:

```bash
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=EarnSafe2026SecretKeyForJWTAuthenticationMustBeAtLeast256BitsLong
export WEATHER_API_KEY=   # optional — mock events work without it
cd backend && mvn spring-boot:run
```

Or with a `.env` loader such as `dotenv` / IntelliJ run configurations.

### Required Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_USERNAME` | MySQL username (default: `root`) | No (defaults to `root`) |
| `DB_PASSWORD` | MySQL password for `earnsafe_db` | **Yes** |
| `JWT_SECRET` | ≥ 256-bit random string for JWT signing | **Yes** |
| `WEATHER_API_KEY` | OpenWeatherMap API key | No (mock events work fine) |
| `APP_SEED_ENABLED` | Seed demo data on first startup (`true`/`false`) | No (defaults to `true`) |

### Frontend — `frontend/.env`

Only public-safe values belong here. Copy `frontend/.env.example` and fill in:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=EarnSafe
```

> Do **not** store secret API keys (database passwords, JWT secrets, payment keys) in the frontend `.env`. Vite embeds all `VITE_` variables into the client bundle — they are visible to anyone who inspects the JavaScript.

> **Never commit `.env` files.** Both are listed in `.gitignore`. Only `.env.example` files (with placeholder values) should be committed.

---

## 10. Database Setup

```sql
-- Run once in MySQL
CREATE DATABASE IF NOT EXISTS earnsafe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Spring Boot will create all tables automatically on first startup (`spring.jpa.hibernate.ddl-auto=update`).

---

## 11. Backend Setup

**Prerequisites:** Java 17+, Maven 3.8+, MySQL 8.0+

```bash
cd backend

# 1. Copy and configure environment
cp .env.example .env
# Fill in DB credentials, JWT secret, and optional API keys

# 2. Confirm port in application.properties (default: 8080)
#    server.port=8080

# 3. Start the backend
mvn spring-boot:run
```

The API is available at `http://localhost:8080/api`.

> **Port note:** `application.properties` currently sets `server.port=8081`. The frontend Vite proxy (`vite.config.js`) targets port `8080`. Update one of these to make them consistent — either change `application.properties` to `server.port=8080`, or change the `proxy.target` in `vite.config.js` to `http://localhost:8081`. Also update `VITE_API_BASE_URL` in `frontend/.env` to use whichever port you choose.

---

## 12. Frontend Setup

**Prerequisites:** Node.js 18+

```bash
cd frontend

# 1. Copy and configure environment
cp .env.example .env
# Default value is already correct for local development:
# VITE_API_BASE_URL=http://localhost:8080/api

# 2. Install dependencies
npm install

# 3. Start development server
npm run dev
```

Frontend will be available at `http://localhost:5173`.

The Vite dev server proxies `/api` requests to `http://localhost:8080`, so both the `VITE_API_BASE_URL` env variable and the Vite proxy must point to the same backend port.

---

## 13. Running the Full Project

Run in two separate terminals:

**Terminal 1 — Backend**
```bash
cd backend
mvn spring-boot:run
# → http://localhost:8080/api
```

**Terminal 2 — Frontend**
```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### Verify Integration

Once both servers are running, verify the following end-to-end flow:

1. Open `http://localhost:5173`.
2. Register a new worker account.
3. Log in — confirm the JWT is stored in `localStorage` (browser DevTools → Application → Local Storage).
4. Navigate to "Buy Policy" — verify the premium is calculated and returned from the backend.
5. Create a policy — confirm it appears in "My Policies" and is stored in MySQL.
6. As admin, fire a trigger event — confirm claims are generated for affected workers.
7. Approve a claim as admin — confirm the status updates and persists on page refresh.

---

## 14. API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register a new worker | No |
| POST | `/api/auth/login` | Login and receive JWT | No |
| GET | `/api/auth/me` | Get current user profile | Yes |

### Policies

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/policies/create` | Create a new policy | Yes (Worker) |
| GET | `/api/policies/my` | List current user's policies | Yes |
| GET | `/api/policies/{id}` | Get policy details | Yes |
| PUT | `/api/policies/{id}/renew` | Renew a policy | Yes |
| PUT | `/api/policies/{id}/pause` | Pause a policy | Yes |
| PUT | `/api/policies/{id}/deactivate` | Deactivate a policy | Yes |

### Premium Engine

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/premium/calculate` | Calculate weekly premium for logged-in user | Yes |
| GET | `/api/premium/explain/{userId}` | Get premium breakdown for a specific user | Yes |

### Claims

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/claims/my` | Get current user's claims | Yes |
| GET | `/api/claims/{id}` | Get claim details | Yes |
| PUT | `/api/claims/{id}/approve` | Approve claim | Yes (Admin) |
| PUT | `/api/claims/{id}/reject` | Reject claim | Yes (Admin) |
| PUT | `/api/claims/{id}/mark-paid` | Mark claim as paid | Yes (Admin) |

### Triggers

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/triggers/live` | List recent weather/disruption events | Yes |
| POST | `/api/triggers/mock-event` | Create a test trigger event | Yes (Authenticated; typically Admin via UI) |
| POST | `/api/triggers/evaluate-all` | Evaluate all active policies against an event | Yes (Authenticated; typically Admin via UI) |
| POST | `/api/triggers/simulate-feed` | Simulate rotating trigger feed across active-policy cities | Yes (Authenticated; typically Admin via UI) |

> `mock-event`, `evaluate-all`, and `simulate-feed` are **test/demo utilities**. The backend also runs a scheduler (`TriggerMonitoringService`) every `app.trigger.interval` to simulate and evaluate trigger events automatically.

### Admin

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/admin/dashboard` | Platform KPIs | Yes (Admin) |
| GET | `/api/admin/users` | All registered users | Yes (Admin) |
| GET | `/api/admin/policies` | All policies | Yes (Admin) |
| GET | `/api/admin/claims` | All claims | Yes (Admin) |
| GET | `/api/admin/risk-zones` | Risk zone configuration | Yes (Admin) |

---

## 15. Authentication Flow

1. User submits credentials to `POST /api/auth/login`.
2. Backend validates credentials, generates a signed JWT (expires in 24 hours by default).
3. Frontend stores the token in `localStorage` and the user object in `localStorage`.
4. `AuthContext.jsx` reads these values on app load to restore session state.
5. Every subsequent API call goes through `axios.js`, which reads the token from `localStorage` and attaches `Authorization: Bearer <token>`.
6. If any response returns `401 Unauthorized`, the interceptor clears storage and redirects to `/login`.
7. On logout, the frontend clears `localStorage` — the JWT is stateless and does not need server-side invalidation.

---

## 16. Data Flow

```
1.  Worker registers  →  POST /auth/register  →  User saved to MySQL
2.  Worker logs in    →  POST /auth/login     →  JWT returned, stored in localStorage
3.  Worker requests premium quote  →  POST /premium/calculate
                                   →  Backend runs risk engine
                                   →  Returns finalWeeklyPremium + breakdown
4.  Worker creates policy  →  POST /policies/create
                           →  Existing active policy is set to INACTIVE (single active policy per user)
                           →  New policy saved as ACTIVE for 1 week (premium + coverage defaults)
5.  Trigger event arrives  →  (manual) /triggers/evaluate-all OR (auto) scheduler run
                           →  Threshold check: rain/flood/heat/pollution/zone-closure
                           →  Active policies in same city are evaluated
                           →  Duplicate claim prevention by user + disruptionDate + triggerType
                           →  Claim auto-created with estimated loss and fraud score
6.  Claim decision path    →  Non-fraud: AUTO_APPROVED + payoutAmount set
                           →  Fraud-flagged: UNDER_REVIEW until admin action
7.  Admin adjudicates      →  PUT /claims/{id}/approve or /reject
                           →  Status persisted in MySQL
8.  Admin pays claim       →  PUT /claims/{id}/mark-paid
                           →  PayoutService sets status=PAID + transactionId (TXN-*)
9.  Worker views dashboard →  GET /policies/my + GET /claims/my
                           →  Live data returned from MySQL
                           →  Frontend renders current state
```

---

## 17. Seeder / Demo Data

The backend includes a `DataSeeder` class at:

```
backend/src/main/java/com/earnsafe/config/DataSeeder.java
```

On startup, if the database is empty, it seeds:
- 1 admin account: `admin@earnsafe.com` / `Admin@123`
- 5 worker accounts: `ravi`, `priya`, `suresh`, `anjali`, `rafiq` at `@example.com` / `Worker@123`
- Sample risk zones, policies, and weather events

The seeder is **guarded** — it only runs when the database has no existing data, so it will not re-seed on subsequent restarts.

### Disabling the Seeder

To start with a clean database (recommended for final demo with real registered users):

**Option A — via `application.properties`:**
```properties
app.seed.enabled=false
```

**Option B — via `backend/.env` (if you migrate `application.properties` to read env vars):**
```env
SEED_ENABLED=false
```

Drop and recreate the database, then restart the backend. The `DataSeeder` checks `app.seed.enabled` before running; with it set to `false`, the application starts with an empty database.

---

## 18. Remove Mock Data Checklist

Use this checklist before submitting the final demo to ensure all data flows through the real database.

### Frontend Cleanup
- [ ] Confirm no page component contains a hardcoded array of users, policies, or claims as its data source
- [ ] Replace any static chart data with values from backend summary endpoints
- [ ] Replace any static status cards with data from `/api/admin/dashboard`
- [ ] Remove any frontend fallback arrays that display when API calls fail in production
- [ ] Confirm `AuthContext.jsx` reads user data from `localStorage` (set by login API), not hardcoded values

### Backend Cleanup
- [ ] Set `app.seed.enabled=false` in `application.properties` when using real registered users
- [ ] Remove or comment out any hardcoded event data in test controllers before final submission

### Definition of Done
- [ ] A new user can register and their record appears in MySQL
- [ ] Login returns a valid JWT and the dashboard loads personal data
- [ ] A policy created from the frontend is stored in MySQL and persists on page refresh
- [ ] Premium is calculated by the backend risk engine, not a hardcoded frontend value
- [ ] Claims table shows only claims fetched from the DB for the logged-in user
- [ ] Admin actions (approve/reject/mark-paid) update the DB and are reflected immediately
- [ ] Frontend page refresh still shows persisted data (no in-memory-only state)

---

## 19. What API Keys Are Required

| Purpose | API / Service | Status | Used For |
|---------|--------------|--------|----------|
| Weather trigger data | OpenWeatherMap API | Pluggable — not yet integrated | Rain, heatwave, wind risk |
| Air quality / pollution | AQI / AirVisual / WAQI API | Pluggable — not yet integrated | Pollution spike trigger |
| Flood / disaster alerts | Government open feeds / NDMA | Pluggable — not yet integrated | Flood / disruption events |
| Geocoding / zone mapping | Google Maps / Mapbox / OpenCage | Pluggable — not yet integrated | Worker zone detection |
| Payments / payouts | Razorpay Test Mode | Pluggable — not yet integrated | Simulated payout processing |
| SMS / notifications | Twilio / Fast2SMS | Pluggable — not yet integrated | Claim and trigger alerts |

**Current state:** The trigger system uses manually fired API calls (`/triggers/mock-event`, `/triggers/evaluate-all`) for demo verification. Live API integrations are architecturally planned (configuration keys exist in `application.properties`) but not yet active. For the hackathon submission, this is sufficient.

> For free-tier API access: OpenWeatherMap, WAQI, and OpenCage all offer generous free tiers suitable for a hackathon demo.

---

## 20. Dynamic Premium Engine

Base premium: **₹39/week**

| Factor | Adjustment |
|--------|-----------|
| High rain risk zone | +₹8 |
| High flood risk zone | +₹12 |
| Heatwave zone | +₹5 |
| High pollution zone | +₹4 |
| Night shift worker | +₹5 |
| High daily earnings (>₹800/day) | +₹6 |
| Grocery/quick-commerce delivery | +₹3 |
| Safe zone discount | −₹5 |

The engine returns a `finalWeeklyPremium`, a `riskScore` (LOW / MEDIUM / HIGH), and an itemized `breakdown` list. Source: `backend/src/main/java/com/earnsafe/service/PremiumService.java`.

---

## 21. Covered Cities & Zones

| City | Zones |
|------|-------|
| Hyderabad | Kukatpally, LB Nagar, Banjara Hills |
| Vijayawada | Governorpet, Benz Circle |
| Visakhapatnam | Gajuwaka, Dwaraka Nagar |
| Guntur | Brodipet |
| Tirupati | Tirumala Road, Renigunta |

Zones and their risk scores are stored in the `risk_zones` table and served via `GET /api/admin/risk-zones`.

---

## 22. Suggested Production Improvements

| Area | Improvement |
|------|------------|
| Security | Move JWT secret to a secrets manager (AWS Secrets Manager, Vault) |
| Security | Enable HTTPS / TLS termination |
| Database | Use connection pooling (HikariCP, already default in Spring Boot) |
| Auth | Add refresh token support and token revocation |
| API | Add rate limiting and request validation middleware |
| Triggers | Connect OpenWeatherMap / AQI APIs for live parametric data |
| Payments | Integrate Razorpay for real premium collection and claims payout |
| Notifications | Add Twilio or Firebase for SMS / push notifications |
| Observability | Add structured logging, metrics (Micrometer + Prometheus), and tracing |
| CI/CD | Add GitHub Actions for build, test, and deploy pipeline |
| Frontend | Add end-to-end tests (Playwright or Cypress) |

---

## 23. Hackathon Relevance — DEVTrails Mapping

| DEVTrails Requirement | EarnSafe Implementation |
|-----------------------|--------------------------|
| Weekly pricing model | Implemented — weekly premium calculated per worker risk profile |
| Income loss coverage only | Implemented — only delivery disruption covered, not health/vehicle |
| Parametric automation | Implemented — claims auto-generated when trigger thresholds are crossed |
| AI-powered premium / risk logic | Implemented — multi-factor risk engine in `PremiumService.java` |
| Fraud detection | Planned next — duplicate claim detection architecture in place |
| Worker dashboard | Implemented — policy, claims, and premium views |
| Admin dashboard | Implemented — KPIs, user management, claim adjudication |

---

## 24. Future Scope

- [ ] Live OpenWeatherMap API integration for real parametric triggers
- [ ] UPI / Razorpay integration for premium collection and payouts
- [ ] WhatsApp / SMS notifications via Twilio or Fast2SMS
- [ ] ML-based fraud detection (duplicate claim flagging)
- [ ] React Native mobile application
- [ ] Multi-language support (Telugu, Hindi)
- [ ] Integration with ONDC gig-worker network
- [ ] Real-time dashboard updates via WebSocket

---

## 25. Recommended Next Files to Update

The following files likely need review or cleanup for a fully production-style integration:

| File | Reason |
|------|--------|
| `backend/src/main/resources/application.properties` | Hardcodes DB password and JWT secret — migrate to environment variables |
| `backend/src/main/java/com/earnsafe/config/DataSeeder.java` | Disable (`app.seed.enabled=false`) before final demo if using real registered users |
| `frontend/src/pages/AdminDashboardPage.jsx` | Verify all KPI cards fetch from `/api/admin/dashboard`, not local state |
| `frontend/src/pages/DashboardPage.jsx` | Verify policies and claims sections call APIs, not static arrays |
| `frontend/src/pages/TriggerMonitorPage.jsx` | Verify live events load from `/api/triggers/live` |
| `frontend/src/context/AuthContext.jsx` | Confirm logout clears localStorage and redirects correctly |
| `frontend/vite.config.js` | Proxy target must match `server.port` in `application.properties` |
| `backend/.env.example` | Create this file with placeholder keys (template committed above) |

---

*Built for Guidewire DEVTrails 2026*
