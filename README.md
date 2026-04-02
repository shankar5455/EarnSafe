# 🛡️ GigShield — AI-Powered Parametric Insurance for India's Gig Delivery Workers

> **Guidewire DEVTrails 2026 · Phase 2 MVP**

GigShield is a full-stack platform that protects gig delivery workers (Swiggy, Zomato, Zepto, Blinkit, Amazon, etc.) from **loss of income** caused by external disruptions like heavy rain, floods, heatwaves, pollution spikes, and zone closures.

---

## 🎯 Problem Statement

India has 15+ million gig delivery workers. When heavy rain hits a city, deliveries stop — but traditional insurance doesn't protect against this kind of **income loss**. GigShield fills this gap with:

- **Parametric triggers**: Automatic claims based on verified weather/event data
- **Weekly micro-premiums**: Starting at ₹39/week
- **Income-focused coverage**: Only for delivery disruptions (not health or vehicle)

---

## ✨ Features

### For Workers
- ✅ Register with delivery platform details
- ✅ View personalized weekly premium with breakdown
- ✅ Activate income protection policy
- ✅ Automatic claim generation on disruption events
- ✅ Track claim status and payouts
- ✅ View live disruption events in their zone

### For Admins
- ✅ Platform-wide dashboard with KPIs
- ✅ Manage workers, policies, and claims
- ✅ Fire simulated trigger events (DEMO MODE)
- ✅ View risk zone data
- ✅ Approve/reject/mark-paid claims

---

## 🔌 5 Parametric Triggers

| Trigger | Condition |
|---------|-----------|
| 🌧️ Heavy Rain | Rainfall > 30mm |
| 🌊 Flood Alert | Flood alert = true |
| 🌡️ Heatwave | Temperature > 42°C |
| 🌫️ Pollution Spike | AQI > 300 |
| 🚧 Zone Closure | Closure alert = true |

---

## 🏗️ Tech Stack

### Frontend
- **React** (with Vite)
- **Tailwind CSS v4**
- **Axios** — HTTP client
- **React Router DOM** — routing
- **Recharts** — charts
- **Lucide React** — icons

### Backend
- **Java Spring Boot 3.2**
- **Spring Security** (JWT authentication)
- **Spring Data JPA**
- **MySQL** database
- **Lombok**
- **Bean Validation**

---

## 📁 Project Structure

```
GigShield/
├── frontend/                    # React + Vite frontend
│   ├── src/
│   │   ├── api/                 # Axios API clients
│   │   ├── components/          # Reusable UI components
│   │   ├── context/             # React context (Auth)
│   │   ├── pages/               # All page components
│   │   └── App.jsx              # Router
│   ├── .env.example
│   └── package.json
│
├── backend/                     # Spring Boot backend
│   ├── src/main/java/com/gigshield/
│   │   ├── config/              # Security config, Data seeder
│   │   ├── controller/          # REST API controllers
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── entity/              # JPA entities
│   │   ├── exception/           # Global exception handler
│   │   ├── repository/          # Spring Data repositories
│   │   ├── security/            # JWT filter & utility
│   │   └── service/             # Business logic
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── .env.example
│   └── pom.xml
│
└── README.md
```

---

## 🚀 Setup Instructions

### Prerequisites
- Node.js >= 18
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Database Setup

```sql
CREATE DATABASE gigshield_db;
```

### 2. Backend Setup

```bash
cd backend

# Copy and configure environment
cp .env.example .env
# Edit .env with your MySQL credentials and JWT secret

# Run the application (auto-seeds database on first start)
mvn spring-boot:run
```

Backend will start at: `http://localhost:8080/api`

> **Note**: Database seeds automatically on first startup with sample users, policies, claims, and weather events.

### 3. Frontend Setup

```bash
cd frontend

# Copy environment
cp .env.example .env
# Edit if backend URL is different from localhost:8080

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start at: `http://localhost:5173`

---

## 🔑 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@gigshield.com | Admin@123 |
| Worker (Ravi - Swiggy, HYD) | ravi@example.com | Worker@123 |
| Worker (Priya - Zomato, VJA) | priya@example.com | Worker@123 |
| Worker (Suresh - Zepto, HYD) | suresh@example.com | Worker@123 |
| Worker (Anjali - Blinkit, VSP) | anjali@example.com | Worker@123 |
| Worker (Rafiq - Amazon, GNT) | rafiq@example.com | Worker@123 |

---

## 📡 API Endpoints

### Authentication
```
POST /api/auth/register    — Register a new worker
POST /api/auth/login       — Login and get JWT token
GET  /api/auth/me          — Get current user info
```

### Policies
```
POST /api/policies/create       — Create a new policy
GET  /api/policies/my           — Get my policies
GET  /api/policies/{id}         — Get policy details
PUT  /api/policies/{id}/renew   — Renew policy
PUT  /api/policies/{id}/pause   — Pause policy
PUT  /api/policies/{id}/deactivate — Deactivate policy
```

### Premium Engine
```
POST /api/premium/calculate     — Calculate premium for logged-in user
GET  /api/premium/explain/{id}  — Get premium breakdown for user
```

### Claims
```
GET /api/claims/my              — Get my claims
GET /api/claims/{id}            — Get claim details
PUT /api/claims/{id}/approve    — Approve claim (admin only)
PUT /api/claims/{id}/reject     — Reject claim (admin only)
PUT /api/claims/{id}/mark-paid  — Mark claim as paid (admin only)
```

### Triggers (Demo Mode)
```
GET  /api/triggers/live         — Get recent weather events
POST /api/triggers/mock-event   — Create a mock event
POST /api/triggers/evaluate-all — Fire trigger for all matching workers
```

### Admin
```
GET /api/admin/dashboard    — Platform KPIs
GET /api/admin/users        — All users
GET /api/admin/policies     — All policies
GET /api/admin/claims       — All claims
GET /api/admin/risk-zones   — Risk zone data
```

---

## 🎬 Demo Flow (Hackathon)

1. **Login as Admin** → View dashboard with seeded data
2. **Go to Trigger Monitor** → Fire a "Heavy Rain" event in Hyderabad / Kukatpally
3. See automatic claim generation for affected workers
4. **Switch to Worker login** → See the claim in dashboard
5. **Admin approves and marks as paid**

---

## 💡 Dynamic Premium Engine

Base premium: **₹39/week**

Adjustments:
- High rain risk zone: +₹8
- High flood risk zone: +₹12
- Heatwave zone: +₹5
- Pollution zone: +₹4
- Night shift: +₹5
- High earner (>₹800/day): +₹6
- Grocery delivery: +₹3
- Safe zone discount: -₹5

---

## 🗺️ Covered Cities & Zones

| City | Zones |
|------|-------|
| Hyderabad | Kukatpally, LB Nagar, Banjara Hills |
| Vijayawada | Governorpet, Benz Circle |
| Visakhapatnam | Gajuwaka, Dwaraka Nagar |
| Guntur | Brodipet |
| Tirupati | Tirumala Road, Renigunta |

---

## 🔮 Future Scope

- [ ] OpenWeatherMap API live integration
- [ ] UPI/payment gateway for premium collection and payouts
- [ ] WhatsApp/SMS notifications for claims
- [ ] ML-based fraud detection
- [ ] Mobile app (React Native)
- [ ] Multi-language support (Telugu, Hindi)
- [ ] Integration with ONDC network

---

## 📸 Screenshots

> *[Screenshots to be added after deployment]*

---

*Built with ❤️ for Guidewire DEVTrails 2026 Hackathon*
