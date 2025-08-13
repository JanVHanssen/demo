
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  reactStrictMode: true,
  
  // Internationalization configuration
  i18n: {
    defaultLocale: 'nl',
    locales: ['nl', 'en', 'fr', 'de'],

  },
  

};

export default nextConfig;