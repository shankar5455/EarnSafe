import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Shield } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';

const PLATFORMS = ['Swiggy', 'Zomato', 'Zepto', 'Blinkit', 'Amazon', 'Flipkart', 'Other'];
const CATEGORIES = ['Food', 'Grocery', 'E-commerce'];
const SHIFTS = ['Morning', 'Afternoon', 'Evening', 'Night'];
const VEHICLES = ['Bike', 'Cycle', 'EV', 'Scooter'];
const CITIES = ['Hyderabad', 'Vijayawada', 'Visakhapatnam', 'Guntur', 'Tirupati', 'Other'];

export default function RegisterPage() {
  const [form, setForm] = useState({
    fullName: '', phone: '', email: '', password: '',
    deliveryPlatform: 'Swiggy', deliveryCategory: 'Food',
    city: 'Hyderabad', zone: '', pincode: '',
    preferredShift: 'Morning', averageDailyEarnings: '', averageWorkingHours: '',
    vehicleType: 'Bike',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const set = (field, val) => setForm((f) => ({ ...f, [field]: val }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await register({
        ...form,
        averageDailyEarnings: parseFloat(form.averageDailyEarnings),
        averageWorkingHours: parseFloat(form.averageWorkingHours),
      });
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const Field = ({ label, children }) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
      {children}
    </div>
  );

  const inputClass = "w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent";

  const Select = ({ field, options }) => (
    <select
      value={form[field]}
      onChange={(e) => set(field, e.target.value)}
      className={inputClass}
    >
      {options.map((o) => <option key={o}>{o}</option>)}
    </select>
  );

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <div className="flex-1 py-12 px-4">
        <div className="max-w-2xl mx-auto">
          <div className="text-center mb-8">
            <div className="flex justify-center mb-3">
              <div className="bg-blue-600 p-3 rounded-2xl">
                <Shield className="h-8 w-8 text-white" />
              </div>
            </div>
            <h1 className="text-2xl font-bold text-gray-800">Create Your Account</h1>
            <p className="text-gray-500 text-sm mt-1">Register as a GigShield protected delivery partner</p>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
                  {error}
                </div>
              )}

              {/* Personal Info */}
              <div>
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-4">Personal Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Field label="Full Name *">
                    <input type="text" value={form.fullName} onChange={(e) => set('fullName', e.target.value)}
                      className={inputClass} placeholder="Ravi Kumar" required />
                  </Field>
                  <Field label="Phone Number *">
                    <input type="tel" value={form.phone} onChange={(e) => set('phone', e.target.value)}
                      className={inputClass} placeholder="9876543210" required />
                  </Field>
                  <Field label="Email Address *">
                    <input type="email" value={form.email} onChange={(e) => set('email', e.target.value)}
                      className={inputClass} placeholder="ravi@example.com" required />
                  </Field>
                  <Field label="Password *">
                    <input type="password" value={form.password} onChange={(e) => set('password', e.target.value)}
                      className={inputClass} placeholder="Min. 6 characters" required minLength={6} />
                  </Field>
                </div>
              </div>

              {/* Delivery Info */}
              <div>
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-4">Delivery Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Field label="Delivery Platform *">
                    <Select field="deliveryPlatform" options={PLATFORMS} />
                  </Field>
                  <Field label="Delivery Category *">
                    <Select field="deliveryCategory" options={CATEGORIES} />
                  </Field>
                  <Field label="Vehicle Type *">
                    <Select field="vehicleType" options={VEHICLES} />
                  </Field>
                  <Field label="Preferred Shift *">
                    <Select field="preferredShift" options={SHIFTS} />
                  </Field>
                </div>
              </div>

              {/* Location */}
              <div>
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-4">Location & Earnings</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Field label="City *">
                    <Select field="city" options={CITIES} />
                  </Field>
                  <Field label="Zone / Area">
                    <input type="text" value={form.zone} onChange={(e) => set('zone', e.target.value)}
                      className={inputClass} placeholder="e.g., Kukatpally" />
                  </Field>
                  <Field label="Pincode">
                    <input type="text" value={form.pincode} onChange={(e) => set('pincode', e.target.value)}
                      className={inputClass} placeholder="500072" />
                  </Field>
                  <Field label="Average Daily Earnings (₹) *">
                    <input type="number" value={form.averageDailyEarnings} onChange={(e) => set('averageDailyEarnings', e.target.value)}
                      className={inputClass} placeholder="650" required min="0" />
                  </Field>
                  <Field label="Average Working Hours/Day *">
                    <input type="number" value={form.averageWorkingHours} onChange={(e) => set('averageWorkingHours', e.target.value)}
                      className={inputClass} placeholder="7" required min="0" max="24" step="0.5" />
                  </Field>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 rounded-xl font-semibold text-sm hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {loading ? 'Creating Account...' : 'Create Account & Get Protected'}
              </button>
            </form>

            <p className="text-center text-sm text-gray-500 mt-6">
              Already have an account?{' '}
              <Link to="/login" className="text-blue-600 font-semibold hover:underline">
                Login here
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
