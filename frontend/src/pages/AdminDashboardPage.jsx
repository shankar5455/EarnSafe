import { useEffect, useState } from 'react';
import { adminApi } from '../api';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import DashboardCard from '../components/DashboardCard';
import StatusBadge from '../components/StatusBadge';
import Loader from '../components/Loader';
import {
  Users, FileText, AlertTriangle, CheckCircle, XCircle, DollarSign, Shield, TrendingUp
} from 'lucide-react';
import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Legend
} from 'recharts';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [claims, setClaims] = useState([]);
  const [riskZones, setRiskZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [dashRes, usersRes, polRes, clmRes, rzRes] = await Promise.allSettled([
          adminApi.getDashboard(),
          adminApi.getUsers(),
          adminApi.getPolicies(),
          adminApi.getClaims(),
          adminApi.getRiskZones(),
        ]);
        if (dashRes.status === 'fulfilled') setStats(dashRes.value.data);
        if (usersRes.status === 'fulfilled') setUsers(usersRes.value.data);
        if (polRes.status === 'fulfilled') setPolicies(polRes.value.data);
        if (clmRes.status === 'fulfilled') setClaims(clmRes.value.data);
        if (rzRes.status === 'fulfilled') setRiskZones(rzRes.value.data);
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading admin dashboard..." /></div>;

  const claimsPieData = stats
    ? Object.entries(stats.claimsByStatus).map(([name, value]) => ({ name, value }))
    : [];

  const triggerBarData = stats
    ? Object.entries(stats.triggerCountByType || {}).map(([name, value]) => ({
        name: name.replace('_', ' '), value
      }))
    : [];

  const tabs = ['overview', 'users', 'policies', 'claims', 'risk-zones'];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-8 space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">⚙️ Admin Dashboard</h1>
          <p className="text-gray-500 text-sm mt-1">EarnSafe platform overview and management</p>
        </div>

        {/* Tab Navigation */}
        <div className="flex gap-2 flex-wrap">
          {tabs.map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 rounded-xl text-sm font-medium capitalize transition-colors ${
                activeTab === tab
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              {tab.replace('-', ' ')}
            </button>
          ))}
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && stats && (
          <>
            {/* KPI Cards */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <DashboardCard title="Total Workers" value={stats.totalWorkers} icon={Users} color="blue" />
              <DashboardCard title="Active Policies" value={stats.activePolicies} icon={Shield} color="green" />
              <DashboardCard title="Total Claims" value={stats.totalClaims} icon={FileText} color="orange" />
              <DashboardCard title="Paid Claims" value={stats.paidClaims} icon={DollarSign} color="purple" />
              <DashboardCard title="Approved Claims" value={stats.approvedClaims} icon={CheckCircle} color="teal" />
              <DashboardCard title="Rejected Claims" value={stats.rejectedClaims} icon={XCircle} color="red" />
              <DashboardCard title="Pending Claims" value={stats.pendingClaims} icon={AlertTriangle} color="orange" />
              <DashboardCard title="High Risk Zones" value={stats.topRiskyZones?.length || 0} icon={TrendingUp} color="red" />
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <h3 className="font-semibold text-gray-700 mb-4">Claims by Status</h3>
                {claimsPieData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={250}>
                    <PieChart>
                      <Pie data={claimsPieData} dataKey="value" nameKey="name" outerRadius={90} label>
                        {claimsPieData.map((_, i) => (
                          <Cell key={i} fill={COLORS[i % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                ) : <p className="text-gray-400 text-sm text-center py-10">No data</p>}
              </div>

              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <h3 className="font-semibold text-gray-700 mb-4">Trigger Frequency by Type</h3>
                {triggerBarData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={250}>
                    <BarChart data={triggerBarData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                ) : <p className="text-gray-400 text-sm text-center py-10">No trigger data</p>}
              </div>
            </div>

            {/* Top Risky Zones */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-semibold text-gray-700 mb-4">Top High-Risk Zones</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {stats.topRiskyZones?.map((zone, i) => (
                  <div key={i} className="flex items-center justify-between bg-red-50 rounded-xl p-4">
                    <div>
                      <p className="font-semibold text-gray-700">{zone.zone}</p>
                      <p className="text-sm text-gray-500">{zone.city}</p>
                    </div>
                    <StatusBadge status={zone.riskLevel} />
                  </div>
                ))}
              </div>
            </div>
          </>
        )}

        {/* Users Tab */}
        {activeTab === 'users' && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-700">All Workers ({users.filter(u => u.role === 'WORKER').length})</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
                  <tr>
                    <th className="px-6 py-4 text-left">Name</th>
                    <th className="px-6 py-4 text-left">Phone</th>
                    <th className="px-6 py-4 text-left">Platform</th>
                    <th className="px-6 py-4 text-left">City / Zone</th>
                    <th className="px-6 py-4 text-left">Shift</th>
                    <th className="px-6 py-4 text-left">Daily Earnings</th>
                    <th className="px-6 py-4 text-left">Role</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium text-gray-700">{u.fullName}</td>
                      <td className="px-6 py-4 text-gray-500">{u.phone}</td>
                      <td className="px-6 py-4 text-gray-600">{u.deliveryPlatform}</td>
                      <td className="px-6 py-4 text-gray-500">{u.city} · {u.zone}</td>
                      <td className="px-6 py-4 text-gray-500">{u.preferredShift}</td>
                      <td className="px-6 py-4 font-medium text-gray-700">₹{u.averageDailyEarnings}</td>
                      <td className="px-6 py-4"><StatusBadge status={u.role} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Policies Tab */}
        {activeTab === 'policies' && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-700">All Policies ({policies.length})</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
                  <tr>
                    <th className="px-6 py-4 text-left">Policy #</th>
                    <th className="px-6 py-4 text-left">Worker</th>
                    <th className="px-6 py-4 text-left">Plan</th>
                    <th className="px-6 py-4 text-left">Premium</th>
                    <th className="px-6 py-4 text-left">Coverage</th>
                    <th className="px-6 py-4 text-left">Zone</th>
                    <th className="px-6 py-4 text-left">Risk</th>
                    <th className="px-6 py-4 text-left">Status</th>
                    <th className="px-6 py-4 text-left">Valid Until</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {policies.map((p) => (
                    <tr key={p.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium text-gray-700">{p.policyNumber}</td>
                      <td className="px-6 py-4 text-gray-600">{p.userFullName}</td>
                      <td className="px-6 py-4 text-gray-500">{p.planName}</td>
                      <td className="px-6 py-4 font-medium">₹{p.weeklyPremium}</td>
                      <td className="px-6 py-4 text-gray-600">₹{p.weeklyCoverageAmount}</td>
                      <td className="px-6 py-4 text-gray-500">{p.zoneCovered}</td>
                      <td className="px-6 py-4"><StatusBadge status={p.riskScore} /></td>
                      <td className="px-6 py-4"><StatusBadge status={p.status} /></td>
                      <td className="px-6 py-4 text-gray-500">{p.endDate}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Claims Tab */}
        {activeTab === 'claims' && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-700">All Claims ({claims.length})</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
                  <tr>
                    <th className="px-6 py-4 text-left">Claim #</th>
                    <th className="px-6 py-4 text-left">Worker</th>
                    <th className="px-6 py-4 text-left">Trigger</th>
                    <th className="px-6 py-4 text-left">Zone</th>
                    <th className="px-6 py-4 text-left">Date</th>
                    <th className="px-6 py-4 text-left">Lost Income</th>
                    <th className="px-6 py-4 text-left">Payout</th>
                    <th className="px-6 py-4 text-left">Fraud</th>
                    <th className="px-6 py-4 text-left">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {claims.map((c) => (
                    <tr key={c.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium text-gray-700">{c.claimNumber}</td>
                      <td className="px-6 py-4 text-gray-600">{c.userFullName}</td>
                      <td className="px-6 py-4 text-gray-600">{c.triggerType?.replace('_', ' ')}</td>
                      <td className="px-6 py-4 text-gray-500">{c.zone}</td>
                      <td className="px-6 py-4 text-gray-500">{c.disruptionDate}</td>
                      <td className="px-6 py-4 font-medium text-red-600">₹{c.estimatedLostIncome?.toFixed(0)}</td>
                      <td className="px-6 py-4 font-medium text-green-600">₹{c.payoutAmount?.toFixed(0)}</td>
                      <td className="px-6 py-4">
                        {c.fraudFlag
                          ? <span className="text-xs bg-red-100 text-red-700 px-2 py-0.5 rounded-full font-medium">⚠️ Yes</span>
                          : <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">✓ No</span>
                        }
                      </td>
                      <td className="px-6 py-4"><StatusBadge status={c.claimStatus} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Risk Zones Tab */}
        {activeTab === 'risk-zones' && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-700">Risk Zone Registry ({riskZones.length})</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
                  <tr>
                    <th className="px-6 py-4 text-left">City</th>
                    <th className="px-6 py-4 text-left">Zone</th>
                    <th className="px-6 py-4 text-center">Flood Risk</th>
                    <th className="px-6 py-4 text-center">Rain Risk</th>
                    <th className="px-6 py-4 text-center">Heat Risk</th>
                    <th className="px-6 py-4 text-center">Pollution Risk</th>
                    <th className="px-6 py-4 text-center">Closure Risk</th>
                    <th className="px-6 py-4 text-center">Overall Level</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {riskZones.map((rz) => (
                    <tr key={rz.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium text-gray-700">{rz.city}</td>
                      <td className="px-6 py-4 text-gray-600">{rz.zone}</td>
                      <td className="px-6 py-4 text-center">
                        <RiskBar score={rz.floodRiskScore} />
                      </td>
                      <td className="px-6 py-4 text-center">
                        <RiskBar score={rz.rainRiskScore} />
                      </td>
                      <td className="px-6 py-4 text-center">
                        <RiskBar score={rz.heatRiskScore} />
                      </td>
                      <td className="px-6 py-4 text-center">
                        <RiskBar score={rz.pollutionRiskScore} />
                      </td>
                      <td className="px-6 py-4 text-center">
                        <RiskBar score={rz.closureRiskScore} />
                      </td>
                      <td className="px-6 py-4 text-center"><StatusBadge status={rz.overallRiskLevel} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}

function RiskBar({ score }) {
  const color = score >= 8 ? 'bg-red-500' : score >= 5 ? 'bg-yellow-500' : 'bg-green-500';
  return (
    <div className="flex items-center justify-center gap-2">
      <div className="w-16 bg-gray-100 rounded-full h-2">
        <div className={`${color} h-2 rounded-full`} style={{ width: `${(score / 10) * 100}%` }} />
      </div>
      <span className="text-xs font-medium text-gray-600">{score}</span>
    </div>
  );
}
