# EarnSafe (AI-Powered Insurance for Gig Workers)

EarnSafe is a production-focused, full-stack insurance platform for delivery workers that uses ML-based risk scoring, automated parametric claims, fraud detection, and instant payouts.

## 1) Problem Statement
Gig workers lose daily income during rain, floods, heatwaves, and city disruptions. Traditional insurance is claim-heavy and slow. EarnSafe automates risk pricing, trigger-based claims, and payout execution to protect worker earnings.

## 2) Features
- AI risk prediction (`/predict-risk`) for premium and risk-level generation
- AI fraud detection (`/detect-fraud`) using anomaly detection
- OpenWeatherMap live ingestion (temperature, rainfall, AQI proxy, severe condition mapping)
- Parametric automation: severe weather + inactive worker → auto-claim → auto-approve → auto-payout
- Stripe test-mode payout integration
- Worker dashboard: premium, risk score, policy, protected earnings
- Admin dashboard: fraud alerts, claims analytics, risk heatmap

## 3) Architecture (Text Diagram)
`React Frontend` → `Spring Boot API` → (`MySQL` + `Python FastAPI AI Service`) → (`OpenWeatherMap API`, `Stripe Test API`)

Backend follows layered architecture:
- Controllers
- Services (business automation, AI integration, weather ingestion, payout orchestration)
- Repositories (JPA persistence)

## 4) Tech Stack
- Spring Boot 3 (Java 17)
- React + Vite
- Python FastAPI + scikit-learn
- MySQL
- OpenWeatherMap API
- Stripe (sandbox/test mode)

## 5) How It Works (Flow)
1. User registers and activates policy
2. Backend requests AI risk prediction for premium
3. Scheduler/admin scan fetches live weather from OpenWeatherMap
4. If weather is severe and worker inactivity is detected, claim triggers automatically
5. Fraud model scores the claim
6. Non-fraud claim is auto-approved and paid through Stripe transfer

## 6) Database Tables
Core + analytics tables:
- `users`, `policies`, `claims`, `risk_zones`, `delivery_activities`, `weather_events`
- `weather_data`
- `risk_scores`
- `fraud_scores`
- `claim_triggers`

## 7) Setup Instructions
### Backend
```bash
cd backend
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=your_256_bit_secret
export WEATHER_API_KEY=your_openweather_key
export STRIPE_API_KEY=your_stripe_test_secret
export STRIPE_DESTINATION_ACCOUNT=acct_test_destination
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

## 8) API Endpoints
### AI Service
- `POST /predict-risk`
- `POST /detect-fraud`
- `GET /health`

### Backend (selected)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/premium/calculate`
- `POST /api/policies/create`
- `GET /api/claims/my`
- `GET /api/triggers/live`
- `POST /api/triggers/scan` (admin)
- `POST /api/triggers/scan-all` (admin)
- `GET /api/admin/dashboard`

## 9) Demo Flow (for Judges)
1. Register worker and buy policy
2. Open worker dashboard to view AI premium and risk score
3. From admin trigger monitor, run city scan or all-city scan
4. Observe auto-generated claim and fraud score
5. Verify automatic payout with transaction ID
6. Open admin dashboard for fraud alerts and heatmap analytics

## 10) Screenshots
- `docs/screenshots/worker-dashboard.png` *(placeholder)*
- `docs/screenshots/admin-dashboard.png` *(placeholder)*
- `docs/screenshots/trigger-monitor.png` *(placeholder)*
- `docs/screenshots/claims-flow.png` *(placeholder)*
