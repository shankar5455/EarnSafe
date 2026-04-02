import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, FileText, Shield } from 'lucide-react';
import { policyApi } from '../api';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import StatusBadge from '../components/StatusBadge';
import Loader from '../components/Loader';
import EmptyState from '../components/EmptyState';

export default function PoliciesPage() {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    policyApi.getMy()
      .then((res) => setPolicies(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading policies..." /></div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-5xl mx-auto w-full px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">My Policies</h1>
            <p className="text-gray-500 text-sm mt-1">Manage your income protection coverage</p>
          </div>
          <Link
            to="/policies/buy"
            className="inline-flex items-center gap-2 bg-blue-600 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-blue-700 transition-colors"
          >
            <Plus className="h-4 w-4" />
            New Policy
          </Link>
        </div>

        {policies.length === 0 ? (
          <EmptyState
            title="No policies yet"
            subtitle="Get your first policy to start income protection"
            action={
              <Link to="/policies/buy" className="inline-flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-xl text-sm font-semibold hover:bg-blue-700">
                <Plus className="h-4 w-4" /> Get Policy
              </Link>
            }
          />
        ) : (
          <div className="space-y-4">
            {policies.map((policy) => (
              <div key={policy.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className="bg-blue-100 p-3 rounded-xl">
                      <Shield className="h-6 w-6 text-blue-600" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-bold text-gray-800">{policy.policyNumber}</span>
                        <StatusBadge status={policy.status} />
                        <StatusBadge status={policy.riskScore} />
                      </div>
                      <p className="text-sm text-gray-500 mt-1">{policy.planName}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        Zone: {policy.zoneCovered} · Valid: {policy.startDate} → {policy.endDate}
                      </p>
                    </div>
                  </div>
                  <div className="flex flex-col sm:items-end gap-1">
                    <p className="text-2xl font-bold text-blue-600">₹{policy.weeklyPremium}</p>
                    <p className="text-xs text-gray-400">per week</p>
                    <p className="text-sm text-gray-600 font-medium">Coverage: ₹{policy.weeklyCoverageAmount}</p>
                  </div>
                  <Link
                    to={`/policies/${policy.id}`}
                    className="shrink-0 bg-gray-100 text-gray-700 text-sm px-4 py-2 rounded-xl hover:bg-gray-200 transition-colors font-medium"
                  >
                    View Details
                  </Link>
                </div>

                <div className="mt-4 pt-4 border-t border-gray-50">
                  <p className="text-xs text-gray-400">
                    <span className="font-medium text-gray-500">Covered Disruptions: </span>
                    {policy.coveredDisruptions?.split(',').map((d) => d.trim()).join(' · ')}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
