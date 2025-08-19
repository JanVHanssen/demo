// pages/index.tsx
import React, { useEffect, useState } from "react";
import { GetStaticProps } from 'next';
import { useTranslation } from 'next-i18next';
import { serverSideTranslations } from 'next-i18next/serverSideTranslations';
import Link from 'next/link';
import { Geist, Geist_Mono } from "next/font/google";
import { getCurrentUser, validateToken, User } from '../services/AuthService';

// Fonts import
const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });
const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] });

const Home: React.FC = () => {
  const { t } = useTranslation('common');
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      const currentUser = getCurrentUser();
      if (currentUser) {
        const isValid = await validateToken();
        if (isValid) {
          setUser(currentUser);
        }
      }
      setLoading(false);
    };
    checkAuth();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`${geistSans.className} min-h-screen flex flex-col bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100`}>

      <main className="flex-grow flex flex-col items-center justify-center p-8 text-center">
        <h1 className="text-5xl font-bold mb-6 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
          {t('hero.welcome')} Car4Rent
        </h1>
        
        <p className="text-xl text-gray-600 dark:text-gray-300 mb-8 max-w-2xl">
          {t('hero.description')}
        </p>

        {!user ? (
          // Show register buttons for non-logged in users
          <div className="flex flex-col sm:flex-row gap-4 mb-12">
            <Link
              href="/register?role=RENTER"
              className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-4 rounded-lg font-semibold text-lg transition-colors"
            >
              🚗 {t('hero.rentCar')}
            </Link>
            
            <Link
              href="/register?role=OWNER"
              className="bg-green-600 hover:bg-green-700 text-white px-8 py-4 rounded-lg font-semibold text-lg transition-colors"
            >
              💰 {t('hero.rentOutCar')}
            </Link>
          </div>
        ) : (
          // Show welcome message for logged in users
          <div className="bg-green-100 border border-green-400 text-green-700 px-6 py-4 rounded-lg mb-12 max-w-2xl">
            <h3 className="text-xl font-semibold mb-2">🎉 Welcome back, {user.username}!</h3>
            <p>You are logged in as: <span className="font-medium">{user.roles?.join(', ')}</span></p>
            <p className="text-sm mt-2">Use the navigation menu above to access your dashboard and features.</p>
            
            {/* Quick action buttons for logged in users */}
            <div className="mt-4 flex flex-wrap gap-2 justify-center">
              <Link
                href="/dashboard"
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded text-sm transition-colors"
              >
                📊 Dashboard
              </Link>
              {user.roles?.includes('ADMIN') && (
                <Link
                  href="/AdminUsers"
                  className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded text-sm transition-colors"
                >
                  👥 Manage Users
                </Link>
              )}
              {(user.roles?.includes('OWNER') || user.roles?.includes('ADMIN')) && (
                <Link
                  href="/Cars"
                  className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded text-sm transition-colors"
                >
                  🚗 Manage Cars
                </Link>
              )}
            </div>
          </div>
        )}

        {/* Features section - always show */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl">
          <div className="text-center p-6">
            <div className="text-4xl mb-4">🔒</div>
            <h3 className="text-xl font-semibold mb-2">{t('features.secure')}</h3>
            <p className="text-gray-600 dark:text-gray-300">
              {t('features.secureDesc')}
            </p>
          </div>
          
          <div className="text-center p-6">
            <div className="text-4xl mb-4">⚡</div>
            <h3 className="text-xl font-semibold mb-2">{t('features.fast')}</h3>
            <p className="text-gray-600 dark:text-gray-300">
              {t('features.fastDesc')}
            </p>
          </div>
          
          <div className="text-center p-6">
            <div className="text-4xl mb-4">💯</div>
            <h3 className="text-xl font-semibold mb-2">{t('features.reliable')}</h3>
            <p className="text-gray-600 dark:text-gray-300">
              {t('features.reliableDesc')}
            </p>
          </div>
        </div>

        {/* Test database buttons - only for logged in users */}
        {user && (
          <div className="mt-8 bg-gray-50 dark:bg-gray-800 rounded-lg p-6 max-w-2xl">
            <h3 className="text-lg font-semibold mb-4">🔧 Quick Tests</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <button
                onClick={() => window.open('/api/health/database', '_blank')}
                className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded transition-colors"
              >
                Test Database
              </button>
              <button
                onClick={() => window.open('/api/health/flyway', '_blank')}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded transition-colors"
              >
                Check Migrations
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export const getStaticProps: GetStaticProps = async ({ locale }) => {
  return {
    props: {
      ...(await serverSideTranslations(locale || 'nl', ['common'])),
    },
  };
};

export default Home;