// pages/index.tsx
import React, { useEffect, useState } from "react";
import { getGreeting } from "../services/HelloService";
import Header from "../components/Header";


const Home: React.FC = () => {
  const [greeting, setGreeting] = useState<string>("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    
    getGreeting()
      .then(setGreeting)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center">
      <Header />
      <main className="text-center mt-8">
        <h1 className="text-4xl font-bold mb-4">Greeting:</h1>
        {error ? (
          <p className="text-red-600 font-medium">Error: {error}</p>
        ) : (
          <p className="text-green-600 text-2xl">{greeting}</p>
        )}
      </main>
    </div>
  );
};

export default Home;
