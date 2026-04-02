import { Link } from 'react-router-dom';
import { Shield, CloudRain, Zap, CheckCircle, ArrowRight, Users, TrendingUp, AlertTriangle } from 'lucide-react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

export default function LandingPage() {
  const features = [
    {
      icon: CloudRain,
      title: 'Parametric Triggers',
      desc: 'Automatic claim payouts when rain, floods, heatwaves, or zone closures disrupt your deliveries.',
      color: 'text-blue-500',
      bg: 'bg-blue-50',
    },
    {
      icon: Zap,
      title: 'Instant Claims',
      desc: 'No paperwork. No waiting. Claims are triggered automatically based on verified weather data.',
      color: 'text-yellow-500',
      bg: 'bg-yellow-50',
    },
    {
      icon: TrendingUp,
      title: 'Dynamic Premium',
      desc: 'Weekly premiums as low as ₹39. Priced based on your zone risk, shift, and earnings profile.',
      color: 'text-green-500',
      bg: 'bg-green-50',
    },
    {
      icon: Shield,
      title: 'Income Protection',
      desc: 'Covers lost income when you can\'t work due to external disruptions — not accidents or health.',
      color: 'text-purple-500',
      bg: 'bg-purple-50',
    },
  ];

  const steps = [
    { step: '01', title: 'Register', desc: 'Sign up with your delivery platform details and work zone.' },
    { step: '02', title: 'Get a Policy', desc: 'Review your personalized weekly premium and activate coverage.' },
    { step: '03', title: 'Stay Protected', desc: 'Live disruption events are monitored for your zone in real time.' },
    { step: '04', title: 'Claim Payout', desc: 'When a trigger event occurs, your claim is processed automatically.' },
  ];

  const platforms = ['Swiggy', 'Zomato', 'Zepto', 'Blinkit', 'Amazon', 'Flipkart'];

  return (
    <div className="min-h-screen bg-white flex flex-col">
      <Navbar />

      {/* Hero Section */}
      <section className="bg-gradient-to-br from-blue-900 via-blue-800 to-indigo-900 text-white py-24 px-4">
        <div className="max-w-5xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 bg-blue-700/50 px-4 py-2 rounded-full text-sm text-blue-200 mb-6">
            <Zap className="h-4 w-4" />
            Guidewire DEVTrails 2026 · Phase 2 MVP
          </div>
          <h1 className="text-5xl md:text-6xl font-extrabold mb-6 leading-tight">
            Protect Your <span className="text-blue-300">Income</span>,<br />
            Not Just Your Vehicle
          </h1>
          <p className="text-xl text-blue-200 mb-10 max-w-3xl mx-auto">
            GigShield is India's first AI-powered parametric income insurance for gig delivery workers.
            When heavy rain, floods, or zone closures stop your deliveries — we pay you automatically.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="inline-flex items-center gap-2 bg-white text-blue-900 px-8 py-4 rounded-xl font-bold text-lg hover:bg-blue-50 transition-all shadow-lg"
            >
              Start Protection <ArrowRight className="h-5 w-5" />
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center gap-2 border-2 border-blue-400 text-white px-8 py-4 rounded-xl font-bold text-lg hover:bg-blue-800 transition-all"
            >
              Login
            </Link>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-8 mt-16 pt-8 border-t border-blue-700/50">
            <div>
              <div className="text-3xl font-bold text-white">₹39/week</div>
              <div className="text-blue-300 text-sm">Starting Premium</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-white">5 Types</div>
              <div className="text-blue-300 text-sm">of Disruptions Covered</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-white">Auto</div>
              <div className="text-blue-300 text-sm">Claim Settlement</div>
            </div>
          </div>
        </div>
      </section>

      {/* Supported Platforms */}
      <section className="py-10 bg-gray-50 border-b">
        <div className="max-w-5xl mx-auto px-4 text-center">
          <p className="text-sm text-gray-500 mb-4 font-medium">FOR DELIVERY PARTNERS OF</p>
          <div className="flex flex-wrap justify-center gap-4">
            {platforms.map((p) => (
              <span key={p} className="bg-white border border-gray-200 text-gray-700 px-5 py-2 rounded-lg text-sm font-semibold shadow-sm">
                {p}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-800 mb-3">Why GigShield?</h2>
            <p className="text-gray-500 max-w-2xl mx-auto">
              Traditional insurance doesn't work for gig workers. GigShield is built specifically for your reality.
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map(({ icon: Icon, title, desc, color, bg }) => (
              <div key={title} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
                <div className={`${bg} p-3 rounded-xl w-fit mb-4`}>
                  <Icon className={`h-6 w-6 ${color}`} />
                </div>
                <h3 className="font-bold text-gray-800 mb-2">{title}</h3>
                <p className="text-gray-500 text-sm leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* What We Cover */}
      <section className="py-16 px-4 bg-gradient-to-br from-slate-50 to-blue-50">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl font-bold text-gray-800 mb-3">What Triggers a Claim?</h2>
          <p className="text-gray-500 mb-10">GigShield monitors 5 types of external disruptions that impact delivery workers.</p>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            {[
              { emoji: '🌧️', label: 'Heavy Rain', threshold: '>30mm' },
              { emoji: '🌊', label: 'Flood Alert', threshold: 'Any Alert' },
              { emoji: '🌡️', label: 'Heatwave', threshold: '>42°C' },
              { emoji: '🌫️', label: 'Pollution Spike', threshold: 'AQI >300' },
              { emoji: '🚧', label: 'Zone Closure', threshold: 'Any Closure' },
            ].map((item) => (
              <div key={item.label} className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 text-center">
                <div className="text-3xl mb-2">{item.emoji}</div>
                <div className="font-semibold text-gray-700 text-sm">{item.label}</div>
                <div className="text-xs text-gray-400 mt-1">{item.threshold}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-20 px-4">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-800 mb-3">How It Works</h2>
            <p className="text-gray-500">Four simple steps to protect your income</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {steps.map(({ step, title, desc }) => (
              <div key={step} className="text-center">
                <div className="w-14 h-14 bg-blue-600 text-white rounded-2xl flex items-center justify-center text-xl font-bold mx-auto mb-4">
                  {step}
                </div>
                <h3 className="font-bold text-gray-800 mb-2">{title}</h3>
                <p className="text-gray-500 text-sm">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-16 px-4 bg-blue-600 text-white text-center">
        <div className="max-w-2xl mx-auto">
          <Shield className="h-12 w-12 mx-auto mb-4 text-blue-200" />
          <h2 className="text-3xl font-bold mb-3">Start Protecting Your Income Today</h2>
          <p className="text-blue-200 mb-8">
            Join thousands of delivery workers protected by GigShield. Just ₹39/week.
          </p>
          <Link
            to="/register"
            className="inline-flex items-center gap-2 bg-white text-blue-700 px-8 py-4 rounded-xl font-bold text-lg hover:bg-blue-50 transition-all"
          >
            Register Now <ArrowRight className="h-5 w-5" />
          </Link>
        </div>
      </section>

      <Footer />
    </div>
  );
}
