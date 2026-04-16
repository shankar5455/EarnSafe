import { useEffect, useState } from 'react';
import { claimsApi, adminApi } from '../api';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import StatusBadge from '../components/StatusBadge';
import Loader from '../components/Loader';
import EmptyState from '../components/EmptyState';

const triggerEmoji = {
  HEAVY_RAIN: '🌧️',
  FLOOD_ALERT: '🌊',
  HEATWAVE: '🌡️',
  POLLUTION_SPIKE: '🌫️',
  ZONE_CLOSURE: '🚧',
};

export default function ClaimsPage() {
  const { isAdmin } = useAuth();
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedClaim, setSelectedClaim] = useState(null);
  const adminMode = isAdmin();

  useEffect(() => {
    const fetchClaims = adminMode ? adminApi.getClaims() : claimsApi.getMy();
    fetchClaims
      .then((res) => setClaims(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load claims'))
      .finally(() => setLoading(false));
  }, [adminMode]);

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading claims..." /></div>;


  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">{adminMode ? 'All Claims' : 'My Claims'}</h1>
          <p className="text-gray-500 text-sm mt-1">Automated processing enabled · fraud-screened claims are system-handled</p>
        </div>

        {error && <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-6">{error}</div>}

        {claims.length === 0 && !error ? (
          <EmptyState title="No claims found" subtitle="Claims are auto-generated when severe weather and inactivity conditions are met." />
        ) : (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr className="text-left text-xs text-gray-500 font-semibold uppercase tracking-wide">
                    <th className="px-6 py-4">Claim</th>
                    {adminMode && <th className="px-6 py-4">Worker</th>}
                    <th className="px-6 py-4">Trigger</th>
                    <th className="px-6 py-4">Zone</th>
                    <th className="px-6 py-4">Date</th>
                    <th className="px-6 py-4">Payout</th>
                    <th className="px-6 py-4">Fraud Score</th>
                    <th className="px-6 py-4">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {claims.map((claim) => (
                    <tr key={claim.id} className="hover:bg-gray-50 cursor-pointer" onClick={() => setSelectedClaim(claim)}>
                      <td className="px-6 py-4 font-medium text-gray-700">{claim.claimNumber}</td>
                      {adminMode && <td className="px-6 py-4 text-gray-600">{claim.userFullName}</td>}
                      <td className="px-6 py-4 text-gray-600">{triggerEmoji[claim.triggerType] || '⚠️'} {claim.triggerType?.replace('_', ' ')}</td>
                      <td className="px-6 py-4 text-gray-500">{claim.zone}</td>
                      <td className="px-6 py-4 text-gray-500">{claim.disruptionDate}</td>
                      <td className="px-6 py-4 font-medium text-green-600">₹{claim.payoutAmount?.toFixed(0)}</td>
                      <td className="px-6 py-4 text-gray-500">{claim.fraudScore !== null && claim.fraudScore !== undefined ? `${(claim.fraudScore * 100).toFixed(0)}%` : '—'}</td>
                      <td className="px-6 py-4"><StatusBadge status={claim.claimStatus} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {selectedClaim && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" onClick={() => setSelectedClaim(null)}>
            <div className="bg-white rounded-2xl max-w-lg w-full p-8 shadow-2xl" onClick={(e) => e.stopPropagation()}>
              <h2 className="text-lg font-bold text-gray-800 mb-4">{selectedClaim.claimNumber}</h2>
              <div className="space-y-2 text-sm">
                <p><span className="text-gray-500">Worker:</span> {selectedClaim.userFullName}</p>
                <p><span className="text-gray-500">Trigger:</span> {selectedClaim.triggerType}</p>
                <p><span className="text-gray-500">Payout:</span> ₹{selectedClaim.payoutAmount?.toFixed(0)}</p>
                <p><span className="text-gray-500">Transaction:</span> {selectedClaim.transactionId || 'Pending'}</p>
                <p><span className="text-gray-500">Fraud reason:</span> {selectedClaim.fraudReason || 'None'}</p>
              </div>
              <button onClick={() => setSelectedClaim(null)} className="w-full mt-6 bg-gray-100 text-gray-700 py-2.5 rounded-xl text-sm font-medium hover:bg-gray-200">Close</button>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
