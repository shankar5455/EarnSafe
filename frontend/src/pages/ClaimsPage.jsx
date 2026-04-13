import { useEffect, useRef, useState } from 'react';
import { claimsApi, adminApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
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
  const toast = useToast();
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedClaim, setSelectedClaim] = useState(null);
  const [actionLoading, setActionLoading] = useState('');
  // Capture admin status at mount time; role cannot change without a full re-login/logout.
  const isAdminRef = useRef(isAdmin());

  useEffect(() => {
    const fetchClaims = isAdminRef.current
      ? adminApi.getClaims()
      : claimsApi.getMy();
    fetchClaims
      .then((res) => setClaims(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load claims'))
      .finally(() => setLoading(false));
  }, []);

  const handleAction = async (action, claimId) => {
    setActionLoading(`${action}-${claimId}`);
    try {
      const fn = { approve: claimsApi.approve, reject: claimsApi.reject, paid: claimsApi.markPaid }[action];
      const res = await fn(claimId);
      setClaims((prev) => prev.map((c) => c.id === res.data.id ? res.data : c));
      if (selectedClaim?.id === claimId) setSelectedClaim(res.data);
      const labels = { approve: 'approved', reject: 'rejected', paid: 'marked as paid' };
      toast.success(`Claim ${labels[action]} successfully`);
    } catch (err) {
      toast.error(err.response?.data?.message || `Failed to ${action} claim`);
    } finally {
      setActionLoading('');
    }
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading claims..." /></div>;

  const adminMode = isAdminRef.current;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">
            {adminMode ? 'All Claims' : 'My Claims'}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            {claims.length} claims · {claims.filter((c) => c.claimStatus === 'APPROVED' || c.claimStatus === 'PAID').length} approved/paid
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-6">
            {error}
          </div>
        )}

        {claims.length === 0 && !error ? (
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
                    {adminMode && <th className="px-6 py-4">Worker</th>}
                    <th className="px-6 py-4">Trigger</th>
                    <th className="px-6 py-4">Zone</th>
                    <th className="px-6 py-4">Date</th>
                    <th className="px-6 py-4">Lost Income</th>
                    <th className="px-6 py-4">Payout</th>
                    <th className="px-6 py-4">Fraud Score</th>
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
                      {adminMode && (
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
                        {claim.fraudScore !== undefined && claim.fraudScore !== null ? (
                          <div className="flex flex-col gap-0.5">
                            <div className="flex items-center gap-1">
                              <div className="w-12 bg-gray-100 rounded-full h-1.5">
                                <div
                                  className={`h-1.5 rounded-full ${claim.fraudScore >= 0.5 ? 'bg-red-500' : claim.fraudScore >= 0.3 ? 'bg-yellow-500' : 'bg-green-500'}`}
                                  style={{ width: `${claim.fraudScore * 100}%` }}
                                />
                              </div>
                              <span className="text-xs text-gray-600">{(claim.fraudScore * 100).toFixed(0)}%</span>
                            </div>
                          </div>
                        ) : <span className="text-xs text-gray-400">—</span>}
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex flex-col gap-1">
                          <StatusBadge status={claim.claimStatus} />
                          {claim.fraudFlag && (
                            <span className="text-xs text-red-500 font-medium">⚠️ Fraud Flag</span>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        {adminMode && (
                          <div className="flex gap-1" onClick={(e) => e.stopPropagation()}>
                            {(claim.claimStatus === 'UNDER_VALIDATION' || claim.claimStatus === 'UNDER_REVIEW') && (
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
                    {selectedClaim.fraudScore !== undefined && selectedClaim.fraudScore !== null && (
                      <p className="text-red-600 text-xs mt-1">
                        Fraud Score: <span className="font-bold">{(selectedClaim.fraudScore * 100).toFixed(0)}%</span>
                        {' '}— {selectedClaim.fraudScore >= 0.5 ? 'Under Review' : 'Low Risk'}
                      </p>
                    )}
                  </div>
                )}
                {selectedClaim.claimStatus === 'UNDER_REVIEW' && (
                  <div className="bg-orange-50 border border-orange-200 rounded-lg p-3">
                    <p className="text-orange-700 text-xs font-medium">🔍 Under Review: This claim is being manually reviewed by our fraud detection team.</p>
                  </div>
                )}
                {selectedClaim.claimStatus === 'PAID' && selectedClaim.transactionId && (
                  <div className="bg-green-50 border border-green-200 rounded-lg p-3">
                    <p className="text-green-700 text-sm font-bold">✅ ₹{selectedClaim.payoutAmount?.toFixed(0)} credited successfully</p>
                    <p className="text-green-600 text-xs mt-1">Transaction ID: <span className="font-mono font-semibold">{selectedClaim.transactionId}</span></p>
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
