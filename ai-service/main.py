import math
from contextlib import asynccontextmanager
from typing import Optional

import numpy as np
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sklearn.ensemble import IsolationForest, RandomForestRegressor


class RiskRequest(BaseModel):
    location_risk: float = Field(alias="locationRisk", default=5.0)
    weather_severity: float = Field(alias="weatherSeverity", default=5.0)
    past_claims: int = Field(alias="pastClaims", default=0)
    worker_activity: float = Field(alias="workerActivity", default=0.5)
    city: Optional[str] = Field(alias="city", default=None)
    zone: Optional[str] = Field(alias="zone", default=None)


class RiskResponse(BaseModel):
    risk_score: float = Field(alias="riskScore")
    premium: float = Field(alias="premium")
    risk_level: str = Field(alias="riskLevel")


class FraudRequest(BaseModel):
    location_risk: float = Field(alias="locationRisk", default=5.0)
    weather_severity: float = Field(alias="weatherSeverity", default=5.0)
    claim_frequency: int = Field(alias="claimFrequency", default=0)
    past_claims: int = Field(alias="pastClaims", default=0)
    worker_activity: float = Field(alias="workerActivity", default=0.5)
    gps_distance_km: float = Field(alias="gpsDistanceKm", default=0.0)
    inactivity_mismatch: int = Field(alias="inactivityMismatch", default=0)
    suspicious_velocity: float = Field(alias="suspiciousVelocity", default=0.0)
    city: Optional[str] = Field(alias="city", default=None)
    zone: Optional[str] = Field(alias="zone", default=None)


class FraudResponse(BaseModel):
    fraud_score: float = Field(alias="fraudScore")
    fraud_flag: bool = Field(alias="fraudFlag")
    reason: str = Field(alias="reason")


risk_model: Optional[RandomForestRegressor] = None
fraud_model: Optional[IsolationForest] = None


def _train_risk_model() -> RandomForestRegressor:
    rng = np.random.RandomState(42)
    n = 1500
    location_risk = rng.uniform(1, 10, n)
    weather_severity = rng.uniform(0, 10, n)
    past_claims = rng.randint(0, 25, n)
    worker_activity = rng.uniform(0, 1, n)

    risk = (
        0.35 * (location_risk * 10)
        + 0.40 * (weather_severity * 10)
        + 0.20 * np.clip(past_claims * 4, 0, 100)
        + 0.05 * (100 - (worker_activity * 100))
        + rng.normal(0, 4, n)
    )
    risk = np.clip(risk, 0, 100)

    x = np.column_stack([location_risk, weather_severity, past_claims, worker_activity])
    model = RandomForestRegressor(n_estimators=180, random_state=42)
    model.fit(x, risk)
    return model


def _train_fraud_model() -> IsolationForest:
    rng = np.random.RandomState(42)
    n_normal = 1300
    n_anom = 180

    normal = np.column_stack([
        rng.uniform(2, 8, n_normal),           # location risk
        rng.uniform(1, 8, n_normal),           # weather severity
        rng.randint(0, 4, n_normal),           # claim frequency
        rng.randint(0, 10, n_normal),          # past claims
        rng.uniform(0.3, 1.0, n_normal),       # worker activity
        rng.uniform(0, 15, n_normal),          # gps distance
        rng.randint(0, 1, n_normal),           # inactivity mismatch
        rng.uniform(0, 15, n_normal),          # suspicious velocity
    ])

    anomalies = np.column_stack([
        rng.uniform(6, 10, n_anom),            # high location risk
        rng.uniform(6, 10, n_anom),            # severe weather
        rng.randint(5, 20, n_anom),            # claim frequency spike
        rng.randint(12, 45, n_anom),           # excessive claims history
        rng.uniform(0.0, 0.4, n_anom),         # low activity
        rng.uniform(50, 800, n_anom),          # gps spoofing distance
        rng.randint(1, 2, n_anom),             # inactivity mismatch true
        rng.uniform(60, 300, n_anom),          # suspicious velocity
    ])

    x = np.vstack([normal, anomalies])
    model = IsolationForest(contamination=0.12, random_state=42)
    model.fit(x)
    return model


@asynccontextmanager
async def lifespan(application: FastAPI):
    global risk_model, fraud_model
    risk_model = _train_risk_model()
    fraud_model = _train_fraud_model()
    yield


app = FastAPI(title="EarnSafe AI Service", version="2.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health():
    return {"status": "healthy", "modelsLoaded": risk_model is not None and fraud_model is not None}


@app.post("/predict-risk", response_model=RiskResponse, response_model_by_alias=True)
async def predict_risk(req: RiskRequest):
    features = np.array([[req.location_risk, req.weather_severity, req.past_claims, req.worker_activity]])
    score = float(risk_model.predict(features)[0])  # type: ignore[union-attr]
    score = max(0.0, min(100.0, score))

    premium = 39.0 + (score * 1.6)
    risk_level = "LOW" if score < 33 else "MEDIUM" if score < 66 else "HIGH"

    return RiskResponse(risk_score=round(score, 2), premium=round(premium, 2), risk_level=risk_level)


@app.post("/detect-fraud", response_model=FraudResponse, response_model_by_alias=True)
async def detect_fraud(req: FraudRequest):
    features = np.array([[
        req.location_risk,
        req.weather_severity,
        req.claim_frequency,
        req.past_claims,
        req.worker_activity,
        req.gps_distance_km,
        req.inactivity_mismatch,
        req.suspicious_velocity,
    ]])

    prediction = fraud_model.predict(features)[0]  # type: ignore[union-attr]
    raw_score = fraud_model.decision_function(features)[0]  # type: ignore[union-attr]

    fraud_score = max(0.0, min(1.0, 0.55 - raw_score))
    fraud_flag = bool(prediction == -1 or fraud_score >= 0.55)

    reasons = []
    if req.gps_distance_km > 100:
        reasons.append("GPS spoofing risk")
    if req.claim_frequency >= 5:
        reasons.append("abnormal claim frequency")
    if req.worker_activity < 0.25 and req.inactivity_mismatch == 1:
        reasons.append("fake inactivity pattern")
    if req.suspicious_velocity > 80:
        reasons.append("suspicious mobility velocity")

    reason = "; ".join(reasons) if reasons else ("model anomaly detected" if fraud_flag else "no fraud indicators")

    return FraudResponse(fraud_score=round(fraud_score, 4), fraud_flag=fraud_flag, reason=reason)
