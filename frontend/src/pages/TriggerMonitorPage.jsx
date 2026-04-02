import { useEffect, useState } from 'react';
import { triggerApi } from '../api';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import StatusBadge from '../components/StatusBadge';
import Loader from '../components/Loader';
import { AlertTriangle, Zap, Play } from 'lucide-react';

const TRIGGER_TYPES = [
  { type: 'HEAVY_RAIN', emoji: '🌧️', label: 'Heavy Rain', defaults: { rainfallMm: 50, temperature: 28, aqi: 120, floodAlert: false, closureAlert: false } },
  { type: 'FLOOD_ALERT', emoji: '🌊', label: 'Flood Alert', defaults: { rainfallMm: 90, temperature: 26, aqi: 150, floodAlert: true, closureAlert: false } },
  { type: 'HEATWAVE', emoji: '🌡️', label: 'Heatwave', defaults: { rainfallMm: 0, temperature: 45, aqi: 180, floodAlert: false, closureAlert: false } },
  { type: 'POLLUTION_SPIKE', emoji: '🌫️', label: 'Pollution Spike', defaults: { rainfallMm: 0, temperature: 32, aqi: 320, floodAlert: false, closureAlert: false } },
  { type: 'ZONE_CLOSURE', emoji: '🚧', label: 'Zone Closure', defaults: { rainfallMm: 0, temperature: 30, aqi: 90, floodAlert: false, closureAlert: true } },
];

const CITIES = [
  { city: 'Hyderabad', zones: ['Kukatpally', 'LB Nagar', 'Banjara Hills'] },
  { city: 'Vijayawada', zones: ['Governorpet', 'Benz Circle'] },
  { city: 'Visakhapatnam', zones: ['Gajuwaka', 'Dwaraka Nagar'] },
  { city: 'Guntur', zones: ['Brodipet'] },
  { city: 'Tirupati', zones: ['Tirumala Road', 'Renigunta'] },
];

export default function TriggerMonitorPage() {
  const { isAdmin } = useAuth();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTrigger, setSelectedTrigger] = useState(TRIGGER_TYPES[0]);
  const [city, setCity] = useState('Hyderabad');
  const [zone, setZone] = useState('Kukatpally');
  const [firing, setFiring] = useState(false);
  const [result, setResult] = useState(null);

  const availableZones = CITIES.find((c) => c.city === city)?.zones || [];

  useEffect(() => {
    triggerApi.getLive()
      .then((res) => setEvents(res.data))
      .finally(() => setLoading(false));
  }, []);

  const handleFireTrigger = async () => {
    setFiring(true);
    setResult(null);
    try {
      const payload = {
        city,
        zone,
        eventType: selectedTrigger.type,
        ...selectedTrigger.defaults,
      };
      const res = await triggerApi.evaluateAll(payload);
      setResult(res.data);
      // Refresh events
      const evtRes = await triggerApi.getLive();
      setEvents(evtRes.data);
    } catch (err) {
      setResult({ error: err.response?.data?.message || 'Failed to fire trigger' });
    } finally {
      setFiring(false);
    }
  };

  const eventEmoji = {
    HEAVY_RAIN: '🌧️', FLOOD_ALERT: '🌊', HEATWAVE: '🌡️', POLLUTION_SPIKE: '🌫️', ZONE_CLOSURE: '🚧',
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading events..." /></div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-6xl mx-auto w-full px-4 py-8 space-y-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <AlertTriangle className="h-6 w-6 text-amber-500" />
            Trigger Monitor
          </h1>
          <p className="text-gray-500 text-sm mt-1">Monitor live disruption events and simulate triggers for demo</p>
        </div>

        {/* Demo Mode - Fire Trigger */}
        <div className="bg-gradient-to-br from-orange-50 to-red-50 border-2 border-orange-200 rounded-2xl p-6">
          <div className="flex items-center gap-2 mb-4">
            <Zap className="h-5 w-5 text-orange-500" />
            <h2 className="font-bold text-orange-800">🚀 DEMO MODE — Fire a Trigger</h2>
          </div>
          <p className="text-orange-700 text-sm mb-6">
            Simulate a disruption event to automatically generate claims for all affected workers in the selected zone.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Event Type</label>
              <select
                value={selectedTrigger.type}
                onChange={(e) => setSelectedTrigger(TRIGGER_TYPES.find((t) => t.type === e.target.value))}
                className="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
                {TRIGGER_TYPES.map((t) => (
                  <option key={t.type} value={t.type}>{t.emoji} {t.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">City</label>
              <select
                value={city}
                onChange={(e) => { setCity(e.target.value); setZone(CITIES.find((c) => c.city === e.target.value)?.zones[0] || ''); }}
                className="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
                {CITIES.map((c) => <option key={c.city}>{c.city}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Zone</label>
              <select
                value={zone}
                onChange={(e) => setZone(e.target.value)}
                className="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
                {availableZones.map((z) => <option key={z}>{z}</option>)}
              </select>
            </div>
          </div>

          {/* Event preview */}
          <div className="bg-white rounded-xl p-4 mb-4 text-sm text-gray-600">
            <p className="font-medium text-gray-700 mb-2">Event Parameters:</p>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
              {Object.entries(selectedTrigger.defaults).map(([k, v]) => (
                <div key={k} className="bg-gray-50 rounded-lg px-3 py-2">
                  <p className="text-xs text-gray-400">{k.replace(/([A-Z])/g, ' $1')}</p>
                  <p className="font-semibold">{String(v)}</p>
                </div>
              ))}
            </div>
          </div>

          <button
            onClick={handleFireTrigger}
            disabled={firing}
            className="inline-flex items-center gap-2 bg-orange-500 text-white px-8 py-3 rounded-xl font-bold text-sm hover:bg-orange-600 disabled:opacity-50 transition-colors"
          >
            <Play className="h-4 w-4" />
            {firing ? 'Firing Trigger...' : `Fire ${selectedTrigger.emoji} ${selectedTrigger.label} in ${city} / ${zone}`}
          </button>

          {/* Result */}
          {result && (
            <div className={`mt-6 rounded-xl p-4 ${result.error ? 'bg-red-50 border border-red-200' : 'bg-green-50 border border-green-200'}`}>
              {result.error ? (
                <p className="text-red-700 font-medium text-sm">❌ {result.error}</p>
              ) : (
                <div>
                  <p className="text-green-700 font-bold text-lg mb-2">
                    ✅ Trigger Fired! {result.triggeredClaims} claim(s) generated
                  </p>
                  {result.claims?.length > 0 && (
                    <div className="space-y-2">
                      {result.claims.map((c, i) => (
                        <div key={i} className="bg-white rounded-lg p-3 text-sm flex items-center justify-between">
                          <div>
                            <span className="font-semibold text-gray-700">{c.claimNumber}</span>
                            <span className="text-gray-500 ml-2">· {c.userFullName}</span>
                          </div>
                          <div className="flex items-center gap-3">
                            <span className="text-red-600 font-medium">Lost: ₹{c.estimatedLostIncome?.toFixed(0)}</span>
                            <span className="text-green-600 font-medium">Payout: ₹{c.payoutAmount?.toFixed(0)}</span>
                            <StatusBadge status={c.claimStatus} />
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Live Events */}
        <div>
          <h2 className="text-lg font-bold text-gray-700 mb-4">Recent Disruption Events</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {events.map((e, i) => (
              <div key={i} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
                <div className="flex items-start gap-3">
                  <span className="text-3xl">{eventEmoji[e.eventType] || '⚠️'}</span>
                  <div className="flex-1">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-gray-800">{e.eventType?.replace('_', ' ')}</span>
                      <span className="text-xs bg-gray-100 text-gray-500 px-2 py-1 rounded-full">{e.sourceType}</span>
                    </div>
                    <p className="text-sm text-gray-500 mt-1">{e.city} · {e.zone}</p>
                    <div className="grid grid-cols-3 gap-2 mt-3">
                      {e.temperature && (
                        <div className="text-center bg-orange-50 rounded-lg p-2">
                          <p className="text-xs text-gray-400">Temp</p>
                          <p className="font-semibold text-sm text-orange-600">{e.temperature}°C</p>
                        </div>
                      )}
                      {e.rainfallMm !== null && e.rainfallMm !== undefined && (
                        <div className="text-center bg-blue-50 rounded-lg p-2">
                          <p className="text-xs text-gray-400">Rain</p>
                          <p className="font-semibold text-sm text-blue-600">{e.rainfallMm}mm</p>
                        </div>
                      )}
                      {e.aqi && (
                        <div className="text-center bg-purple-50 rounded-lg p-2">
                          <p className="text-xs text-gray-400">AQI</p>
                          <p className="font-semibold text-sm text-purple-600">{e.aqi}</p>
                        </div>
                      )}
                    </div>
                    <div className="flex gap-3 mt-3">
                      {e.floodAlert && <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-medium">🌊 Flood Alert</span>}
                      {e.closureAlert && <span className="text-xs bg-red-100 text-red-700 px-2 py-0.5 rounded-full font-medium">🚧 Zone Closed</span>}
                    </div>
                    <p className="text-xs text-gray-400 mt-2">
                      {e.eventTimestamp ? new Date(e.eventTimestamp).toLocaleString('en-IN') : 'Recent'}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
}
