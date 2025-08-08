// pages/index.tsx
import React, { useEffect, useState } from "react";
import { getGreeting } from "../services/HelloService";
import Header from "../components/Header";
import { Geist, Geist_Mono } from "next/font/google";

// Fonts import
const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });
const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] });


const Home: React.FC = () => {
  const [greeting, setGreeting] = useState<string>("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getGreeting()
      .then(setGreeting)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div className={`${geistSans.className} min-h-screen flex flex-col bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100`}>


      {/* Content */}
      <main className="flex-grow flex flex-col items-center justify-center p-8 text-center">
        <h1 className="text-4xl font-bold mb-4">Greeting:</h1>
        {error ? (
          <p className="text-red-500 text-lg">{error}</p>
        ) : (
          <p className="text-green-600 text-2xl">{greeting}</p>
        )}
      </main>
    </div>
  );
};

export default Home;
