import { useEffect, useState } from 'react';
import { triggerApi } from '../api';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Loader from '../components/Loader';
import { CloudRain, Cpu } from 'lucide-react';

export default function TriggerMonitorPage() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [city, setCity] = useState('Hyderabad');
  const [scanning, setScanning] = useState(false);
  const [scanResult, setScanResult] = useState(null);

  const loadEvents = async () => {
    const res = await triggerApi.getLive();
    setEvents(res.data);
  };

  useEffect(() => {
    loadEvents().finally(() => setLoading(false));
  }, []);

  const runScan = async () => {
    setScanning(true);
    setScanResult(null);
    try {
      const res = await triggerApi.scanCity(city);
      setScanResult(res.data);
      await loadEvents();
    } catch (err) {
      setScanResult({ error: err.response?.data?.message || 'Weather scan failed' });
    } finally {
      setScanning(false);
    }
  };

  const runScanAll = async () => {
    setScanning(true);
    setScanResult(null);
    try {
      const res = await triggerApi.scanAll();
      setScanResult(res.data);
      await loadEvents();
    } catch (err) {
      setScanResult({ error: err.response?.data?.message || 'Weather scan failed' });
    } finally {
      setScanning(false);
    }
  };

  if (loading) return <div className="min-h-screen bg-gray-50"><Navbar /><Loader text="Loading weather events..." /></div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-6xl mx-auto w-full px-4 py-8 space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Real-Time Trigger Monitor</h1>
          <p className="text-gray-500 text-sm mt-1">Live OpenWeather ingestion and automatic claim automation scans.</p>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <input
              value={city}
              onChange={(e) => setCity(e.target.value)}
              className="border border-gray-300 rounded-xl px-4 py-3 text-sm"
              placeholder="Enter city"
            />
            <button
              onClick={runScan}
              disabled={scanning}
              className="bg-blue-600 text-white rounded-xl px-4 py-3 text-sm font-semibold disabled:opacity-50 inline-flex items-center justify-center gap-2"
            >
              <CloudRain className="h-4 w-4" />
              {scanning ? 'Scanning...' : 'Scan City'}
            </button>
            <button
              onClick={runScanAll}
              disabled={scanning}
              className="bg-indigo-600 text-white rounded-xl px-4 py-3 text-sm font-semibold disabled:opacity-50 inline-flex items-center justify-center gap-2"
            >
              <Cpu className="h-4 w-4" />
              {scanning ? 'Scanning...' : 'Scan All Active Cities'}
            </button>
          </div>

          {scanResult && (
            <div className={`rounded-xl p-4 text-sm ${scanResult.error ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-green-50 text-green-700 border border-green-200'}`}>
              {scanResult.error || `Scan complete: ${scanResult.triggeredClaims || 0} claim(s) generated`}
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {events.map((e, i) => (
            <div key={i} className="bg-white rounded-2xl border border-gray-100 p-5">
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-gray-800">{e.eventType?.replace('_', ' ')}</h3>
                <span className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded-full">{e.sourceType}</span>
              </div>
              <p className="text-sm text-gray-500 mt-1">{e.city} · {e.zone}</p>
              <div className="grid grid-cols-3 gap-2 mt-3 text-sm">
                <div className="bg-orange-50 rounded-lg p-2 text-center">{e.temperature ?? '-'}°C</div>
                <div className="bg-blue-50 rounded-lg p-2 text-center">{e.rainfallMm ?? '-'}mm</div>
                <div className="bg-purple-50 rounded-lg p-2 text-center">AQI {e.aqi ?? '-'}</div>
              </div>
            </div>
          ))}
        </div>
      </main>
      <Footer />
    </div>
  );
}
