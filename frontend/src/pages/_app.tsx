// src/pages/_app.tsx
import '@/styles/globals.css';
import type { AppProps } from 'next/app';
import Header from '@/components/Header';

export default function App({ Component, pageProps }: AppProps) {
  return (
    <>
      <Header />
      <main className="pt-16 max-w-7xl mx-auto px-4">
        <Component {...pageProps} />
      </main>
    </>
  );
}
