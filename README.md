# EarnSafe — AI-Powered Parametric Insurance for Gig Workers

## 1) Project Overview
EarnSafe is a startup-grade parametric micro-insurance platform built for delivery workers. It prices weekly policies with AI risk scoring, auto-detects disruption triggers from live weather, auto-creates claims, runs fraud screening, and executes payout with reliability fallback for demo and production resilience.

## 2) Problem Statement
Gig workers lose daily income during heavy rain, flood alerts, heatwaves, pollution spikes, and closure conditions. Traditional claims are manual, slow, and subjective. EarnSafe turns this into an automated **income-loss protection** product using parametric triggers and AI-assisted underwriting + fraud control.

## 3) Architecture Diagram (Text Explanation)
`React Frontend (Worker/Admin Dashboards)`  
→ `Spring Boot API (Auth, Policy, Premium, Trigger, Claim, Payout orchestration)`  
→ `MySQL (Core + AI audit data)`  
→ `FastAPI AI Service (/predict-risk, /detect-fraud)`  
→ External APIs: `OpenWeatherMap` (event ingestion), `Stripe` (payout transfer)

Core backend layers:
- Controllers: API contracts
- Services: pricing, fraud, trigger automation, payout reliability, AI integration
- Repositories: JPA persistence and analytics queries

## 4) AI Components
- **Risk AI** (`/predict-risk`): generates risk score + risk level + premium signal
- **Fraud AI** (`/detect-fraud`): produces fraud score + reason
- **Rule-enhanced fraud controls** in backend:
  - location mismatch threshold checks
  - high claim velocity in 24h / 7d
  - unrealistic delivery activity pattern checks

## 5) Workflow (End-to-End)
1. Worker registers and creates weekly policy  
2. Premium engine invokes AI risk model and stores risk history  
3. Scheduler/admin trigger scan ingests city weather and evaluates severe parametric events  
4. Matching workers with inactivity are auto-claimed  
5. Fraud evaluation returns score, reason, flag; values are stored and exposed in APIs/UI  
6. Approved claims attempt Stripe payout  
7. If Stripe fails, fallback marks claim as `SIMULATED_SUCCESS`, stores failure reason, and tracks retry status  
8. Scheduled retry reconciles pending fallback payouts when Stripe becomes available

## 6) Features
- Weekly policy pricing model
- Income-loss focused payout computation (lost hours/income)
- AI risk score + risk level in policy/premium views
- AI + rule-based fraud scoring with reason traceability
- Fully automated parametric claim lifecycle
- Reliable payout flow with fallback and retry status tracking
- Worker + Admin dashboards with analytics visibility

## 7) Fraud Detection Strategy
- AI model score + backend rule amplification
- Location validation via worker city vs trigger event distance threshold
- Claim velocity anomaly checks:
  - too many claims in last 24h
  - high claim count in 7-day window
- Unrealistic activity checks:
  - high deliveries with very low login hours
  - zero deliveries with unusually high claimed activity/income signal
- Output fields:
  - `fraudScore`
  - `fraudReason`
  - `fraudFlag`

## 8) Parametric Trigger Logic
Claims are automatically generated only when:
- Severe weather/event condition is detected (rainfall, flood, heatwave, AQI spike, closure pattern)
- Worker inactivity condition is met
- Duplicate claim guard for same worker + event type + date is not violated

This ensures objective trigger-based automation and prevents manual claim bias.

## 9) Payout Flow (with Fallback Reliability)
- Primary mode: Stripe transfer
- Reliability controls:
  - Stripe call wrapped with error handling
  - On failure: claim is marked paid via `SIMULATED_SUCCESS` mode for continuity
  - Fallback metadata persisted:
    - payout status
    - retry pending flag
    - retry count
    - failure reason
  - Scheduled reconciliation retries pending payouts up to configured max attempts

## 10) Security Improvements
- Removed hardcoded secrets from source configuration
- Sensitive values now use environment variables:
  - `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET`
  - `WEATHER_API_KEY`
  - `STRIPE_API_KEY`, `STRIPE_DESTINATION_ACCOUNT`
- Fraud and payout outcomes are auditable in DB for operations and governance.

## 11) Demo-Ready Judge Flow
1. Register a worker (`/api/auth/register`)
2. Create policy (`/api/policies/create`)
3. View AI premium + risk level (`/api/premium/calculate`, dashboard)
4. Run weather trigger scan (`/api/triggers/scan` or `/api/triggers/scan-all`)
5. Observe auto-generated claim
6. Verify fraud score/reason in claim and admin views
7. Verify payout outcome:
   - Stripe success (`STRIPE_SUCCESS`) or
   - fallback simulation (`SIMULATED_SUCCESS`) with retry state

## 12) Setup Instructions
### Backend
```bash
cd backend
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=your_256_bit_secret
export WEATHER_API_KEY=your_openweather_key
export STRIPE_API_KEY=your_stripe_test_secret
export STRIPE_DESTINATION_ACCOUNT=acct_test_destination
export PAYOUT_RETRY_MAX_ATTEMPTS=3
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### AI Service
```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

## 13) Basic Automated Testing
- Added Spring Boot integration tests:
  - `AuthServiceSpringBootTest` (registration/auth path)
  - `PremiumServiceSpringBootTest` (AI premium service path)

Run:
```bash
cd backend
mvn test
```

## 14) Final Verification Checklist
- Weekly pricing model → **YES**
- Income loss only payout logic → **YES**
- AI integration → **YES**
- Fully automated claims → **YES**
- Payout reliability fallback + retry → **FIXED**

## 15) Future Enhancements
- Device fingerprinting and attestation for stronger identity integrity
- Geo-fencing with precise worker GPS ingestion (consented)
- Event streaming + dead-letter queue for payout retries at scale
- Explainable AI dashboard timelines for underwriting and fraud audits
- Multi-provider payout failover beyond Stripe
