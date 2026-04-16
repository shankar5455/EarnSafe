import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, CheckCircle, Info } from 'lucide-react';
import { premiumApi, policyApi } from '../api';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Loader from '../components/Loader';

const PLANS = [
  { name: 'Basic Protection Plan', multiplier: 1, hours: 32, label: 'Essential' },
  { name: 'Standard Weekly Plan', multiplier: 1.2, hours: 40, label: 'Recommended' },
  { name: 'Premium Shield Plan', multiplier: 1.5, hours: 48, label: 'Best Coverage' },
];

const DISRUPTIONS_ALL = 'HEAVY_RAIN,FLOOD_ALERT,HEATWAVE,POLLUTION_SPIKE,ZONE_CLOSURE';

export default function BuyPolicyPage() {
  const [premium, setPremium] = useState(null);
  const [selectedPlan, setSelectedPlan] = useState(1);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    premiumApi.calculate()
      .then((res) => setPremium(res.data))
      .finally(() => setLoading(false));
  }, []);

  const handleBuy = async () => {
    if (!premium) return;
    setSubmitting(true);
    setError('');
    try {
      const plan = PLANS[selectedPlan];
      const weeklyPremium = (parseFloat(premium.finalWeeklyPremium) * plan.multiplier).toFixed(2);
      await policyApi.create({
        planName: plan.name,
        weeklyPremium: parseFloat(weeklyPremium),
        weeklyCoverageAmount: parseFloat(weeklyPremium) * 10,
        coveredHours: plan.hours,
        coveredDisruptions: DISRUPTIONS_ALL,
        zoneCovered: user?.zone || user?.city,
      });
      navigate('/policies');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create policy');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Calculating your premium..." /></div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-4xl mx-auto w-full px-4 py-8">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-gray-800">Get Income Protection</h1>
          <p className="text-gray-500 text-sm mt-1">Choose your plan and activate your weekly coverage</p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-6">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          {PLANS.map((plan, index) => {
            const planPremium = premium ? (parseFloat(premium.finalWeeklyPremium) * plan.multiplier).toFixed(0) : '--';
            const isSelected = selectedPlan === index;
            return (
              <div
                key={plan.name}
                onClick={() => setSelectedPlan(index)}
                className={`bg-white rounded-2xl border-2 p-6 cursor-pointer transition-all ${
                  isSelected ? 'border-blue-500 shadow-lg shadow-blue-100' : 'border-gray-200 hover:border-blue-200'
                }`}
              >
                {plan.label === 'Recommended' && (
                  <div className="bg-blue-600 text-white text-xs px-3 py-1 rounded-full w-fit mb-3 font-semibold">
                    ⭐ Recommended
                  </div>
                )}
                <h3 className="font-bold text-gray-800 mb-1">{plan.name}</h3>
                <div className="flex items-end gap-1 my-3">
                  <span className="text-3xl font-bold text-blue-600">₹{planPremium}</span>
                  <span className="text-gray-400 text-sm pb-1">/week</span>
                </div>
                <p className="text-gray-500 text-sm mb-4">Coverage: ₹{planPremium ? (parseInt(planPremium) * 10).toLocaleString('en-IN') : '--'}</p>
                <ul className="space-y-2">
                  <li className="flex items-center gap-2 text-sm text-gray-600">
                    <CheckCircle className="h-4 w-4 text-green-500 shrink-0" />
                    {plan.hours} covered hours/week
                  </li>
                  <li className="flex items-center gap-2 text-sm text-gray-600">
                    <CheckCircle className="h-4 w-4 text-green-500 shrink-0" />
                    All 5 disruption types
                  </li>
                  <li className="flex items-center gap-2 text-sm text-gray-600">
                    <CheckCircle className="h-4 w-4 text-green-500 shrink-0" />
                    Auto claim settlement
                  </li>
                  <li className="flex items-center gap-2 text-sm text-gray-600">
                    <CheckCircle className="h-4 w-4 text-green-500 shrink-0" />
                    Zone: {user?.zone || user?.city}
                  </li>
                </ul>
                {isSelected && (
                  <div className="mt-4 text-center">
                    <span className="text-blue-600 font-semibold text-sm">✓ Selected</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Premium Breakdown */}
        {premium && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-8">
            <h3 className="font-semibold text-gray-700 mb-4 flex items-center gap-2">
              <Info className="h-4 w-4 text-blue-500" />
              Your Premium Breakdown
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                {premium.breakdown?.map((item, i) => {
                  const amt = parseFloat(item.amount);
                  const isBase = i === 0;
                  const isDiscount = amt < 0;
                  const colorClass = isDiscount ? 'text-green-600' : isBase ? 'text-gray-800' : 'text-red-500';
                  const display = isBase ? `₹${Math.abs(amt)}` : `${isDiscount ? '-' : '+'}₹${Math.abs(amt)}`;
                  return (
                    <div key={i} className="flex justify-between text-sm">
                      <span className="text-gray-500">{item.factor}</span>
                      <span className={`font-medium ${colorClass}`}>{display}</span>
                    </div>
                  );
                })}
                <div className="flex justify-between font-bold pt-2 border-t border-gray-200">
                  <span className="text-gray-800">Weekly Premium</span>
                  <span className="text-blue-600 text-lg">₹{premium.finalWeeklyPremium}</span>
                </div>
              </div>
              <div className="bg-blue-50 rounded-xl p-4 h-fit space-y-3">
                <p className="text-sm font-medium text-blue-700">
                  Risk Level: <span className={`font-bold ${premium.riskScore === 'HIGH' ? 'text-red-600' : premium.riskScore === 'LOW' ? 'text-green-600' : 'text-amber-600'}`}>{premium.riskScore}</span>
                </p>
                {premium.riskScoreNumeric !== undefined && premium.riskScoreNumeric !== null && (
                  <p className="text-xs text-blue-600">Model Score: {(premium.riskScoreNumeric * 100).toFixed(0)}%</p>
                )}
                <p className="text-sm text-blue-600">{premium.explanation}</p>
                {premium.basePremium && (
                  <div className="bg-white rounded-lg p-3 text-center">
                    <p className="text-xs text-gray-400 mb-1">Base weekly premium</p>
                    <p className="font-bold text-2xl text-blue-700">₹{premium.basePremium}</p>
                    <p className="text-xs text-gray-400">+ risk adjustments</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        <div className="text-center">
          <button
            onClick={handleBuy}
            disabled={submitting}
            className="inline-flex items-center gap-2 bg-blue-600 text-white px-10 py-4 rounded-xl font-bold text-lg hover:bg-blue-700 transition-colors disabled:opacity-50 shadow-lg"
          >
            <Shield className="h-5 w-5" />
            {submitting ? 'Activating Policy...' : `Activate ${PLANS[selectedPlan].name}`}
          </button>
          <p className="text-xs text-gray-400 mt-3">
            Policy activates immediately · Valid for 7 days · Cancel anytime
          </p>
        </div>
      </main>
      <Footer />
    </div>
  );
}
