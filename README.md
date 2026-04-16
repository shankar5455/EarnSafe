![Build](https://img.shields.io/badge/build-passing-brightgreen)
![AI](https://img.shields.io/badge/AI-powered-blue)
![DEVTrails](https://img.shields.io/badge/DEVTrails-Phase%203-orange)
![License](https://img.shields.io/badge/license-MIT-green)
# 🚀 EarnSafe — AI-Powered Income Protection for Gig Workers

## 📌 Problem Statement
Gig delivery workers can lose daily income when severe weather or disruption events reduce delivery activity. Traditional insurance claims are manual, slow, and subjective. Workers need a fast, objective, and reliable way to protect earnings during disruption days.

EarnSafe is focused specifically on **income-loss protection** for delivery workers.

---

## 💡 Solution Overview
EarnSafe is a full-stack parametric insurance platform that automates the full protection cycle:

- **AI-based underwriting** calculates weekly premium and risk level
- **Parametric triggers** use weather/event data to auto-detect disruptions
- **Automated claims engine** creates and processes claims without manual filing
- **Fraud intelligence** combines ML anomaly detection and rule checks
- **Automated payout orchestration** uses Stripe with reliability fallback + retries

---

## 🧠 Key Features
- **Weekly Insurance Model**
  - Premium and coverage are designed around weekly gig income cycles
- **AI-Based Premium Calculation**
  - FastAPI ML service predicts risk score and premium
- **Automated Parametric Claims**
  - Claims are generated from trigger conditions, not manual forms
- **Intelligent Fraud Detection**
  - AI fraud score + backend rule validation
- **Instant Payout System (with fallback + retry)**
  - Stripe first, then simulated-success fallback with retry reconciliation
- **Worker & Admin Dashboards**
  - Worker: policy/claim/payout visibility
  - Admin: platform risk, claims, trigger, and payout analytics

---

## ⚙️ System Architecture
EarnSafe uses a modular architecture:

- **Frontend (React + Vite)**
  - Worker and admin dashboards
  - Route protection and JWT-based session handling
- **Backend (Spring Boot, Java 17)**
  - Auth, policy, premium, trigger scanning, claims, fraud checks, payouts
  - Scheduled automation for weather scans and payout retries
- **AI Service (FastAPI, Python)**
  - `/predict-risk` for premium risk prediction
  - `/detect-fraud` for anomaly-based fraud scoring
- **Database (MySQL)**
  - Users, policies, weather events, claims, risk/fraud scores, payout states
- **External APIs**
  - **OpenWeather API** for weather conditions and trigger events
  - **Stripe API** for payout transfer attempts

---

## 🤖 AI Components
EarnSafe includes production-style AI decisioning in two core paths:

- **Risk Prediction (Premium Calculation)**
  - Inputs: location risk, weather severity, claims history, worker activity
  - Output: risk score, risk level, and premium signal
  - Used to produce final weekly premium shown in worker/admin dashboards

- **Fraud Detection (Anomaly + Rules)**
  - AI service generates fraud score/flag/reason
  - Backend adds rule checks for location mismatch, claim velocity, and unrealistic activity
  - Final fraud decision determines auto-approval vs rejection path

- **Decision Impact**
  - AI directly influences premium pricing and claim validation outcomes
  - Fraud and risk outputs are persisted for auditability and analytics

---

## 🔄 End-to-End Workflow
1. User registers and completes worker profile.
2. Policy is created under weekly pricing.
3. AI service calculates risk and premium.
4. Weather/event trigger is detected via scan.
5. Claim is auto-generated for eligible worker-policy matches.
6. Fraud check runs (AI + rule amplification).
7. Payout is processed via Stripe; fallback + retry handles failures.

---

## 🌦️ Parametric Trigger System
EarnSafe uses objective weather/event triggers to automate claim initiation:

- Trigger scan fetches live weather/event data for active-policy cities
- Severe conditions are evaluated (heavy rain, flood alerts, heat, pollution spikes, closure)
- Worker inactivity signal is validated
- Duplicate claim guard prevents same worker/event/date duplicates
- Eligible claims are auto-created and processed

**No manual claim filing is required.**

---

## 🛡️ Fraud Detection Strategy
Fraud handling is layered for reliability:

- **Duplicate prevention** at claim creation (user + disruption date + trigger type)
- **Location/activity validation**
  - Distance mismatch checks
  - 24h and 7-day claim velocity checks
  - Unrealistic delivery/activity pattern checks
- **AI anomaly detection**
  - IsolationForest-based fraud model returns fraud score + reason
- **Final decision logic**
  - AI output and rules are merged to produce final fraud flag

---

## 💸 Payout System
EarnSafe payout pipeline prioritizes continuity and reliability:

- Primary payout via **Stripe transfer**
- If Stripe call fails, claim is still marked paid via **`SIMULATED_SUCCESS` fallback**
- Failure context is persisted (`payoutFailureReason`, retry flags, retry count)
- Scheduled reconciliation retries pending payouts (`retryPendingPayouts`) up to configured max attempts

### Reliability Highlights
- No claim gets stuck due to transient payout gateway failure
- Retry metadata enables transparent recovery and ops tracking

---

## 📊 Dashboards
### Worker Dashboard:
- Active and historical policies
- Claims with fraud score and payout status
- Earnings protected / payouts received
- AI premium breakdown and current risk level

### Admin Dashboard:
- Total workers, active policies, claim statuses, payouts
- Fraud detection counts and analytics views
- Trigger frequency by disruption type
- Risk zone visibility and high-risk insights

---

## 🔗 API Integrations
- **OpenWeather API**
  - Weather ingestion for automated trigger evaluation
- **Stripe API**
  - Payout transfer execution in approved claims pipeline

---

## 🧪 Testing
Backend includes Spring Boot integration tests for core business flows:

- `AuthServiceSpringBootTest` — registration and token flow
- `PremiumServiceSpringBootTest` — AI-driven premium calculation behavior

Reliability validation is built into payout orchestration and scheduler flow:

- Fallback mode persistence on Stripe failure
- Scheduled retry reconciliation for pending payouts

---

## 🛠️ Tech Stack
- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Frontend:** React, Vite, Tailwind CSS, Recharts
- **AI:** Python, FastAPI, scikit-learn, NumPy
- **DB:** MySQL

---

## 🚀 How to Run the Project

### 1) Backend Setup
```bash
cd /home/runner/work/EarnSafe/EarnSafe/backend

export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=your_secure_jwt_secret
export WEATHER_API_KEY=your_openweather_api_key
export STRIPE_API_KEY=your_stripe_secret_key
export STRIPE_DESTINATION_ACCOUNT=acct_xxx
export PAYOUT_RETRY_MAX_ATTEMPTS=3

mvn spring-boot:run
```
Backend runs on: `http://localhost:8081/api`

### 2) Frontend Setup
```bash
cd /home/runner/work/EarnSafe/EarnSafe/frontend
npm install
npm run dev
```
Frontend runs on: `http://localhost:5173`

### 3) AI Service Setup
```bash
cd /home/runner/work/EarnSafe/EarnSafe/ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
AI service runs on: `http://localhost:8000`

### 4) Environment Variables (Backend)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `WEATHER_API_KEY`
- `STRIPE_API_KEY`
- `STRIPE_DESTINATION_ACCOUNT`
- `PAYOUT_RETRY_MAX_ATTEMPTS`

---

## 🔐 Security
- JWT-based authentication and protected API routes
- Role-based access control for admin-only operations
- Sensitive config is environment-driven (no hardcoded secrets in app config)

---

## 📈 Future Enhancements
- GPS/device-grade fraud verification for stronger anti-spoofing
- Dynamic online risk learning from production claim outcomes
- Advanced payout reconciliation and multi-gateway failover

---

## 🎯 DEVTrails Phase 3 Compliance
- Weekly pricing → **YES**
- Income loss only → **YES**
- AI integration → **YES**
- Automated claims → **YES**
- Fraud detection → **YES**
- Instant payout → **YES**

---

## 🏁 Conclusion
EarnSafe delivers a startup-grade, AI-powered, fully automated income-protection platform for gig workers—combining parametric triggers, fraud-aware decisioning, and resilient payout reliability into one production-ready system.
