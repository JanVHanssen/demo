module.exports = {
  i18n: {
    defaultLocale: 'nl',
    locales: ['nl', 'en', 'fr', 'de'],
    localeDetection: true,
  },
  react: {
    useSuspense: false,
  },
  reloadOnPrerender: process.env.NODE_ENV === 'development',
};