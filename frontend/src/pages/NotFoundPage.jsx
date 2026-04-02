import { Link } from 'react-router-dom';
import { Shield } from 'lucide-react';
import Navbar from '../components/Navbar';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <div className="flex-1 flex flex-col items-center justify-center text-center px-4">
        <div className="text-8xl mb-6">🛡️</div>
        <h1 className="text-6xl font-extrabold text-blue-600 mb-3">404</h1>
        <h2 className="text-2xl font-bold text-gray-800 mb-3">Page Not Found</h2>
        <p className="text-gray-500 mb-8 max-w-md">
          The page you're looking for doesn't exist or has been moved.
          Your income protection is still active though!
        </p>
        <div className="flex gap-4">
          <Link
            to="/"
            className="bg-blue-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-blue-700 transition-colors"
          >
            Go Home
          </Link>
          <Link
            to="/dashboard"
            className="bg-white border border-gray-200 text-gray-700 px-6 py-3 rounded-xl font-semibold hover:bg-gray-50 transition-colors"
          >
            Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}
