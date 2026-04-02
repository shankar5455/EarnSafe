import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import PoliciesPage from './pages/PoliciesPage';
import BuyPolicyPage from './pages/BuyPolicyPage';
import PolicyDetailPage from './pages/PolicyDetailPage';
import ClaimsPage from './pages/ClaimsPage';
import TriggerMonitorPage from './pages/TriggerMonitorPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/dashboard" element={
            <ProtectedRoute><DashboardPage /></ProtectedRoute>
          } />
          <Route path="/policies" element={
            <ProtectedRoute><PoliciesPage /></ProtectedRoute>
          } />
          <Route path="/policies/buy" element={
            <ProtectedRoute><BuyPolicyPage /></ProtectedRoute>
          } />
          <Route path="/policies/:id" element={
            <ProtectedRoute><PolicyDetailPage /></ProtectedRoute>
          } />
          <Route path="/claims" element={
            <ProtectedRoute><ClaimsPage /></ProtectedRoute>
          } />
          <Route path="/triggers" element={
            <ProtectedRoute><TriggerMonitorPage /></ProtectedRoute>
          } />
          <Route path="/admin" element={
            <ProtectedRoute adminOnly><AdminDashboardPage /></ProtectedRoute>
          } />

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
