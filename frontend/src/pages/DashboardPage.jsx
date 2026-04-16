import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Shield, AlertTriangle, TrendingUp, CheckCircle, Clock, DollarSign, Zap, Plus } from 'lucide-react';
import { policyApi, premiumApi, claimsApi, triggerApi } from '../api';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import DashboardCard from '../components/DashboardCard';
import StatusBadge from '../components/StatusBadge';
import Loader from '../components/Loader';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

export default function DashboardPage() {
  const { user } = useAuth();
  const [policies, setPolicies] = useState([]);
  const [claims, setClaims] = useState([]);
  const [premium, setPremium] = useState(null);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [polRes, clmRes, preRes, evtRes] = await Promise.allSettled([
          policyApi.getMy(),
          claimsApi.getMy(),
          premiumApi.calculate(),
          triggerApi.getLive(),
        ]);
        if (polRes.status === 'fulfilled') setPolicies(polRes.value.data);
        if (clmRes.status === 'fulfilled') setClaims(clmRes.value.data);
        if (preRes.status === 'fulfilled') setPremium(preRes.value.data);
        if (evtRes.status === 'fulfilled') setEvents(evtRes.value.data.slice(0, 5));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const activePolicy = policies.find((p) => p.status === 'ACTIVE');
  const approvedClaims = claims.filter((c) => c.claimStatus === 'APPROVED' || c.claimStatus === 'PAID');
  const totalPayout = approvedClaims.reduce((sum, c) => sum + (c.payoutAmount || 0), 0);
  const latestFraudScoredClaim = claims.find((c) => c.fraudScore !== null && c.fraudScore !== undefined);
  const simulatedFallbackCount = claims.filter((c) => c.payoutStatus === 'SIMULATED_SUCCESS').length;

  const claimStatusData = [
    { name: 'Approved', value: claims.filter((c) => c.claimStatus === 'APPROVED').length },
    { name: 'Paid', value: claims.filter((c) => c.claimStatus === 'PAID').length },
    { name: 'Pending', value: claims.filter((c) => c.claimStatus === 'TRIGGERED' || c.claimStatus === 'UNDER_VALIDATION' || c.claimStatus === 'UNDER_REVIEW').length },
    { name: 'Rejected', value: claims.filter((c) => c.claimStatus === 'REJECTED').length },
  ].filter((d) => d.value > 0);

  const eventEmoji = {
    HEAVY_RAIN: '🌧️',
    FLOOD_ALERT: '🌊',
    HEATWAVE: '🌡️',
    POLLUTION_SPIKE: '🌫️',
    ZONE_CLOSURE: '🚧',
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading your dashboard..." /></div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-8 space-y-8">

        {/* Welcome */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">
              Hello, {user?.fullName?.split(' ')[0]} 👋
            </h1>
            <p className="text-gray-500 text-sm mt-1">
              {user?.deliveryPlatform} · {user?.city} · {user?.zone}
            </p>
          </div>
          {!activePolicy && (
            <Link
              to="/policies/buy"
              className="inline-flex items-center gap-2 bg-blue-600 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-blue-700 transition-colors"
            >
              <Plus className="h-4 w-4" />
              Get Policy
            </Link>
          )}
        </div>

        {/* Active Policy Banner */}
        {activePolicy ? (
          <div className="bg-gradient-to-r from-blue-600 to-indigo-700 rounded-2xl p-6 text-white">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Shield className="h-5 w-5 text-blue-200" />
                  <span className="text-blue-200 text-sm font-medium">Active Policy</span>
                </div>
                <p className="font-bold text-lg">{activePolicy.policyNumber}</p>
                <p className="text-blue-200 text-sm">{activePolicy.planName}</p>
              </div>
              <div className="text-right">
                <p className="text-3xl font-bold">₹{activePolicy.weeklyPremium}</p>
                <p className="text-blue-200 text-sm">per week</p>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold">₹{activePolicy.weeklyCoverageAmount}</p>
                <p className="text-blue-200 text-sm">coverage</p>
              </div>
              <div>
                <span className="bg-green-400/30 text-green-100 text-xs px-3 py-1.5 rounded-full font-semibold">
                  Risk: {activePolicy.riskScore}
                </span>
              </div>
              <Link to={`/policies/${activePolicy.id}`}
                className="bg-white/20 hover:bg-white/30 text-white text-sm px-4 py-2 rounded-xl transition-colors font-medium">
                View Details
              </Link>
            </div>
          </div>
        ) : (
          <div className="bg-amber-50 border border-amber-200 rounded-2xl p-6">
            <div className="flex items-center gap-3">
              <AlertTriangle className="h-8 w-8 text-amber-500" />
              <div>
                <p className="font-semibold text-amber-800">No Active Policy</p>
                <p className="text-amber-600 text-sm">You're not currently protected. Get a policy to start earning income protection.</p>
              </div>
              <Link to="/policies/buy" className="ml-auto bg-amber-500 text-white px-5 py-2 rounded-xl text-sm font-semibold hover:bg-amber-600">
                Get Protected
              </Link>
            </div>
          </div>
        )}

        {/* Stats Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
          <DashboardCard
            title="Weekly Premium"
            value={premium ? `₹${premium.finalWeeklyPremium}` : activePolicy ? `₹${activePolicy.weeklyPremium}` : 'N/A'}
            subtitle="Current pricing"
            icon={TrendingUp}
            color="blue"
          />
          <DashboardCard
            title="Total Claims"
            value={claims.length}
            subtitle={`${approvedClaims.length} approved`}
            icon={CheckCircle}
            color="green"
          />
          <DashboardCard
            title="Earnings Protected"
            value={`₹${totalPayout.toFixed(0)}`}
            subtitle="Total payouts received"
            icon={DollarSign}
            color="purple"
          />
          <DashboardCard
            title="Risk Level"
            value={premium?.riskScore || activePolicy?.riskScore || 'N/A'}
            subtitle={premium?.riskScoreNumeric !== undefined ? `Model Score: ${(premium.riskScoreNumeric * 100).toFixed(0)}%` : 'Model-based risk assessment'}
            icon={Zap}
            color={premium?.riskScore === 'HIGH' ? 'red' : premium?.riskScore === 'LOW' ? 'green' : 'orange'}
          />
          <DashboardCard
            title="Fraud Score"
            value={latestFraudScoredClaim?.fraudScore !== undefined && latestFraudScoredClaim?.fraudScore !== null ? `${(latestFraudScoredClaim.fraudScore * 100).toFixed(0)}%` : 'N/A'}
            subtitle={`Fallback payouts: ${simulatedFallbackCount}`}
            icon={AlertTriangle}
            color={latestFraudScoredClaim?.fraudScore >= 0.65 ? 'red' : 'orange'}
          />
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Claims Status Chart */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-semibold text-gray-700 mb-4">Claims by Status</h3>
            {claimStatusData.length > 0 ? (
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie data={claimStatusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                    {claimStatusData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No claims yet</div>
            )}
          </div>

          {/* Recent Events */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-semibold text-gray-700 mb-4">Recent Disruption Events</h3>
            <div className="space-y-3">
              {events.length > 0 ? events.map((e, i) => (
                <div key={i} className="flex items-center gap-3 p-3 rounded-xl bg-gray-50">
                  <span className="text-xl">{eventEmoji[e.eventType] || '⚠️'}</span>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-700">{e.eventType?.replace('_', ' ')}</p>
                    <p className="text-xs text-gray-400">{e.city} · {e.zone}</p>
                  </div>
                  <span className="text-xs text-gray-400 shrink-0">
                    {e.eventTimestamp ? new Date(e.eventTimestamp).toLocaleDateString('en-IN') : 'Recent'}
                  </span>
                </div>
              )) : (
                <div className="text-center py-8 text-gray-400 text-sm">No recent events</div>
              )}
            </div>
          </div>
        </div>

        {/* Premium Breakdown */}
        {premium && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-semibold text-gray-700 mb-4">Premium Breakdown</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                {premium.breakdown?.map((item, i) => (
                  <div key={i} className="flex justify-between items-center py-2 border-b border-gray-50">
                    <span className="text-sm text-gray-600">{item.factor}</span>
                    <span className={`text-sm font-semibold ${item.amount < 0 ? 'text-green-600' : 'text-gray-800'}`}>
                      {item.amount < 0 ? '-' : '+'}₹{Math.abs(item.amount)}
                    </span>
                  </div>
                ))}
                <div className="flex justify-between items-center pt-2">
                  <span className="font-bold text-gray-800">Total Weekly Premium</span>
                  <span className="font-bold text-blue-600 text-lg">₹{premium.finalWeeklyPremium}</span>
                </div>
              </div>
              <div className="bg-blue-50 rounded-xl p-4">
                <p className="text-sm text-blue-700 font-medium mb-2">💡 Why this premium?</p>
                <p className="text-sm text-blue-600">{premium.explanation}</p>
              </div>
            </div>
          </div>
        )}

        {/* Recent Claims */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-semibold text-gray-700">Recent Claims</h3>
            <Link to="/claims" className="text-blue-600 text-sm hover:underline">View all</Link>
          </div>
          {claims.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-gray-400 text-xs border-b">
                    <th className="pb-3 font-medium">Claim #</th>
                    <th className="pb-3 font-medium">Trigger</th>
                    <th className="pb-3 font-medium">Date</th>
                    <th className="pb-3 font-medium">Amount</th>
                    <th className="pb-3 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {claims.slice(0, 5).map((c) => (
                    <tr key={c.id}>
                      <td className="py-3 font-medium text-gray-700">{c.claimNumber}</td>
                      <td className="py-3 text-gray-600">{c.triggerType?.replace('_', ' ')}</td>
                      <td className="py-3 text-gray-500">{c.disruptionDate}</td>
                      <td className="py-3 font-medium text-gray-700">₹{c.estimatedLostIncome?.toFixed(0)}</td>
                      <td className="py-3"><StatusBadge status={c.claimStatus} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-400 text-sm text-center py-8">No claims yet</p>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
}
