import { Shield } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300 py-10 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-2 text-white">
            <Shield className="h-6 w-6 text-blue-400" />
            <span className="font-bold text-lg">GigShield</span>
          </div>
          <p className="text-sm text-gray-400">
            AI-Powered Parametric Insurance for India's Gig Delivery Workers
          </p>
          <p className="text-xs text-gray-500">
            © 2026 GigShield · Guidewire DEVTrails 2026
          </p>
        </div>
      </div>
    </footer>
  );
}
