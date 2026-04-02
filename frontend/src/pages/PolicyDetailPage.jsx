import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Shield, Calendar, MapPin, RefreshCw, Pause, XCircle } from 'lucide-react';
import { policyApi } from '../api';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import StatusBadge from '../components/StatusBadge';
import Loader from '../components/Loader';

export default function PolicyDetailPage() {
  const { id } = useParams();
  const [policy, setPolicy] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    policyApi.getById(id)
      .then((res) => setPolicy(res.data))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAction = async (action) => {
    setActionLoading(action);
    try {
      const res = await { renew: policyApi.renew, pause: policyApi.pause, deactivate: policyApi.deactivate }[action](id);
      setPolicy(res.data);
    } finally {
      setActionLoading('');
    }
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading policy..." /></div>;
  if (!policy) return null;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-3xl mx-auto w-full px-4 py-8">
        <button onClick={() => navigate(-1)} className="text-sm text-blue-600 hover:underline mb-6 block">
          ← Back to Policies
        </button>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-8 text-white">
            <div className="flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <Shield className="h-6 w-6 text-blue-200" />
                  <span className="text-blue-200 text-sm">Policy Certificate</span>
                </div>
                <h1 className="text-2xl font-bold">{policy.policyNumber}</h1>
                <p className="text-blue-200 mt-1">{policy.planName}</p>
                <p className="text-blue-200 text-sm mt-1">Worker: {policy.userFullName}</p>
              </div>
              <div className="text-right">
                <StatusBadge status={policy.status} />
                <div className="mt-3">
                  <span className="text-3xl font-bold">₹{policy.weeklyPremium}</span>
                  <span className="text-blue-200 text-sm">/week</span>
                </div>
              </div>
            </div>
          </div>

          {/* Details */}
          <div className="p-8 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide">Coverage Details</h3>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Coverage Amount</span>
                <span className="font-semibold text-gray-800">₹{policy.weeklyCoverageAmount}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Covered Hours/Week</span>
                <span className="font-semibold text-gray-800">{policy.coveredHours} hours</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Risk Score</span>
                <StatusBadge status={policy.riskScore} />
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Zone Covered</span>
                <span className="font-semibold text-gray-800 flex items-center gap-1">
                  <MapPin className="h-3 w-3" />
                  {policy.zoneCovered}
                </span>
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide">Validity</h3>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Start Date</span>
                <span className="font-semibold text-gray-800 flex items-center gap-1">
                  <Calendar className="h-3 w-3" />
                  {policy.startDate}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">End Date</span>
                <span className="font-semibold text-gray-800 flex items-center gap-1">
                  <Calendar className="h-3 w-3" />
                  {policy.endDate}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Status</span>
                <StatusBadge status={policy.status} />
              </div>
            </div>

            <div className="md:col-span-2">
              <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3">Covered Disruptions</h3>
              <div className="flex flex-wrap gap-2">
                {policy.coveredDisruptions?.split(',').map((d) => (
                  <span key={d} className="bg-blue-50 text-blue-700 text-xs px-3 py-1.5 rounded-full font-medium">
                    {d.trim().replace('_', ' ')}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="px-8 pb-8">
            <div className="flex flex-wrap gap-3">
              <button
                onClick={() => handleAction('renew')}
                disabled={!!actionLoading}
                className="flex items-center gap-2 bg-green-500 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-green-600 disabled:opacity-50 transition-colors"
              >
                <RefreshCw className="h-4 w-4" />
                {actionLoading === 'renew' ? 'Renewing...' : 'Renew Policy'}
              </button>
              <button
                onClick={() => handleAction('pause')}
                disabled={!!actionLoading || policy.status === 'PAUSED'}
                className="flex items-center gap-2 bg-yellow-500 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-yellow-600 disabled:opacity-50 transition-colors"
              >
                <Pause className="h-4 w-4" />
                {actionLoading === 'pause' ? 'Pausing...' : 'Pause Policy'}
              </button>
              <button
                onClick={() => handleAction('deactivate')}
                disabled={!!actionLoading || policy.status === 'INACTIVE'}
                className="flex items-center gap-2 bg-red-500 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-red-600 disabled:opacity-50 transition-colors"
              >
                <XCircle className="h-4 w-4" />
                {actionLoading === 'deactivate' ? 'Deactivating...' : 'Deactivate'}
              </button>
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
}
