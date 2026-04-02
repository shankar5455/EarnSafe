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
  const [selectedClaim, setSelectedClaim] = useState(null);
  const [actionLoading, setActionLoading] = useState('');

  useEffect(() => {
    const fetchClaims = isAdmin()
      ? adminApi.getClaims()
      : claimsApi.getMy();
    fetchClaims
      .then((res) => setClaims(res.data))
      .finally(() => setLoading(false));
  }, []);

  const handleAction = async (action, claimId) => {
    setActionLoading(`${action}-${claimId}`);
    try {
      const fn = { approve: claimsApi.approve, reject: claimsApi.reject, paid: claimsApi.markPaid }[action];
      const res = await fn(claimId);
      setClaims((prev) => prev.map((c) => c.id === res.data.id ? res.data : c));
      if (selectedClaim?.id === claimId) setSelectedClaim(res.data);
    } finally {
      setActionLoading('');
    }
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading claims..." /></div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">
            {isAdmin() ? 'All Claims' : 'My Claims'}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            {claims.length} claims · {claims.filter((c) => c.claimStatus === 'APPROVED' || c.claimStatus === 'PAID').length} approved/paid
          </p>
        </div>

        {claims.length === 0 ? (
          <EmptyState
            title="No claims found"
            subtitle="Claims are created automatically when disruption events occur in your zone."
          />
        ) : (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr className="text-left text-xs text-gray-500 font-semibold uppercase tracking-wide">
                    <th className="px-6 py-4">Claim</th>
                    {isAdmin() && <th className="px-6 py-4">Worker</th>}
                    <th className="px-6 py-4">Trigger</th>
                    <th className="px-6 py-4">Zone</th>
                    <th className="px-6 py-4">Date</th>
                    <th className="px-6 py-4">Lost Income</th>
                    <th className="px-6 py-4">Payout</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {claims.map((claim) => (
                    <tr
                      key={claim.id}
                      className="hover:bg-gray-50 cursor-pointer"
                      onClick={() => setSelectedClaim(claim)}
                    >
                      <td className="px-6 py-4 font-medium text-gray-700">{claim.claimNumber}</td>
                      {isAdmin() && (
                        <td className="px-6 py-4 text-gray-600">{claim.userFullName}</td>
                      )}
                      <td className="px-6 py-4">
                        <span className="flex items-center gap-2">
                          {triggerEmoji[claim.triggerType] || '⚠️'}
                          <span className="text-gray-600">{claim.triggerType?.replace('_', ' ')}</span>
                        </span>
                      </td>
                      <td className="px-6 py-4 text-gray-500">{claim.zone}</td>
                      <td className="px-6 py-4 text-gray-500">{claim.disruptionDate}</td>
                      <td className="px-6 py-4 font-medium text-gray-700">₹{claim.estimatedLostIncome?.toFixed(0)}</td>
                      <td className="px-6 py-4 font-medium text-green-600">₹{claim.payoutAmount?.toFixed(0)}</td>
                      <td className="px-6 py-4">
                        <div className="flex flex-col gap-1">
                          <StatusBadge status={claim.claimStatus} />
                          {claim.fraudFlag && (
                            <span className="text-xs text-red-500 font-medium">⚠️ Fraud Flag</span>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        {isAdmin() && (
                          <div className="flex gap-1" onClick={(e) => e.stopPropagation()}>
                            {claim.claimStatus === 'UNDER_VALIDATION' && (
                              <>
                                <button
                                  onClick={() => handleAction('approve', claim.id)}
                                  disabled={!!actionLoading}
                                  className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-lg hover:bg-green-200 font-medium disabled:opacity-50"
                                >
                                  Approve
                                </button>
                                <button
                                  onClick={() => handleAction('reject', claim.id)}
                                  disabled={!!actionLoading}
                                  className="text-xs bg-red-100 text-red-700 px-2 py-1 rounded-lg hover:bg-red-200 font-medium disabled:opacity-50"
                                >
                                  Reject
                                </button>
                              </>
                            )}
                            {claim.claimStatus === 'APPROVED' && (
                              <button
                                onClick={() => handleAction('paid', claim.id)}
                                disabled={!!actionLoading}
                                className="text-xs bg-purple-100 text-purple-700 px-2 py-1 rounded-lg hover:bg-purple-200 font-medium disabled:opacity-50"
                              >
                                Mark Paid
                              </button>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Claim Detail Modal */}
        {selectedClaim && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" onClick={() => setSelectedClaim(null)}>
            <div className="bg-white rounded-2xl max-w-lg w-full p-8 shadow-2xl" onClick={(e) => e.stopPropagation()}>
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h2 className="text-lg font-bold text-gray-800">{selectedClaim.claimNumber}</h2>
                  <p className="text-gray-500 text-sm">Policy: {selectedClaim.policyNumber}</p>
                </div>
                <StatusBadge status={selectedClaim.claimStatus} />
              </div>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Worker</span>
                  <span className="font-medium">{selectedClaim.userFullName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Trigger Type</span>
                  <span className="font-medium">{triggerEmoji[selectedClaim.triggerType]} {selectedClaim.triggerType?.replace('_', ' ')}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Zone</span>
                  <span className="font-medium">{selectedClaim.city} · {selectedClaim.zone}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Disruption Date</span>
                  <span className="font-medium">{selectedClaim.disruptionDate}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Lost Hours</span>
                  <span className="font-medium">{selectedClaim.estimatedLostHours}h</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Lost Income</span>
                  <span className="font-medium text-red-600">₹{selectedClaim.estimatedLostIncome?.toFixed(0)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Payout Amount</span>
                  <span className="font-bold text-green-600">₹{selectedClaim.payoutAmount?.toFixed(0)}</span>
                </div>
                {selectedClaim.fraudFlag && (
                  <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                    <p className="text-red-700 text-xs font-medium">⚠️ Fraud Flag: {selectedClaim.fraudReason}</p>
                  </div>
                )}
              </div>
              <button
                onClick={() => setSelectedClaim(null)}
                className="w-full mt-6 bg-gray-100 text-gray-700 py-2.5 rounded-xl text-sm font-medium hover:bg-gray-200"
              >
                Close
              </button>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
