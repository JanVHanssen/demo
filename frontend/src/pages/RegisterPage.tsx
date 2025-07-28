import { useState } from "react";
import { register } from "../services/AuthService";
import Header from "../components/Header"; // Header importeren

export default function RegisterPage() {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [error, setError] = useState<string>("");

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const response = await register(username, password);
      setMessage(response);
      setError("");
    } catch (err) {
      setError("Registratie mislukt");
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">
      {/* Header bovenaan */}
      <header className="p-6 bg-white border-b border-gray-300 shadow">
        <Header />
      </header>

      {/* Registratieformulier gecentreerd */}
      <main className="flex-grow flex items-center justify-center">
        <form
          onSubmit={handleSubmit}
          className="bg-white p-8 rounded shadow-md w-full max-w-sm"
        >
          <h2 className="text-2xl font-bold mb-6 text-center">Registreer</h2>
          {message && <p className="text-green-600 mb-4">{message}</p>}
          {error && <p className="text-red-500 mb-4">{error}</p>}
          <input
            type="text"
            placeholder="Gebruikersnaam"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full mb-4 p-2 border rounded"
          />
          <input
            type="password"
            placeholder="Wachtwoord"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full mb-4 p-2 border rounded"
          />
          <button
            type="submit"
            className="w-full bg-green-600 text-white p-2 rounded hover:bg-green-700"
          >
            Registreer
          </button>
        </form>
      </main>
    </div>
  );
}
