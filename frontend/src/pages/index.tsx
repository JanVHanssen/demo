// pages/index.tsx
import React from "react";
import { GetStaticProps } from 'next';
import { useTranslation } from 'next-i18next';
import { serverSideTranslations } from 'next-i18next/serverSideTranslations';
import Link from 'next/link';
import { Geist, Geist_Mono } from "next/font/google";

// Fonts import
const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });
const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] });

const Home: React.FC = () => {
  const { t } = useTranslation('common');

  return (
    <div className={`${geistSans.className} min-h-screen flex flex-col bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100`}>

      <main className="flex-grow flex flex-col items-center justify-center p-8 text-center">
        <h1 className="text-5xl font-bold mb-6 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
          {t('hero.welcome')} Car4Rent
        </h1>
        
        <p className="text-xl text-gray-600 dark:text-gray-300 mb-8 max-w-2xl">
          {t('hero.description')}
        </p>

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

        {/* Features */}
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