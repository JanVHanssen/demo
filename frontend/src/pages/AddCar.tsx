import React, { useState } from "react";
import Header from "../components/Header";
import { addCar } from "../services/CarService";

export default function AddCarPage() {
  const [brand, setBrand] = useState("");
  const [model, setModel] = useState("");
  const [licensePlate, setLicensePlate] = useState("");
  const [ownerEmail, setOwnerEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage("");
    setError("");

    try {
      await addCar({ brand, model, licensePlate, ownerEmail });
      setMessage("Auto succesvol toegevoegd!");
      setBrand("");
      setModel("");
      setLicensePlate("");
      setOwnerEmail("");
    } catch (err) {
      setError((err as Error).message || "Fout bij toevoegen van auto");
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">
      <header className="p-6 bg-white border-b border-gray-300 shadow">
        <Header />
      </header>

      <main className="flex-grow flex items-center justify-center">
        <form
          onSubmit={handleSubmit}
          className="bg-white p-8 rounded shadow-md w-full max-w-md"
        >
          <h2 className="text-2xl font-bold mb-6 text-center">Voeg een auto toe</h2>

          {message && <p className="text-green-600 mb-4">{message}</p>}
          {error && <p className="text-red-500 mb-4">{error}</p>}

          <input
            type="text"
            placeholder="Merk"
            value={brand}
            onChange={(e) => setBrand(e.target.value)}
            className="w-full mb-4 p-2 border rounded"
            required
          />
          <input
            type="text"
            placeholder="Model"
            value={model}
            onChange={(e) => setModel(e.target.value)}
            className="w-full mb-4 p-2 border rounded"
            required
          />
          <input
            type="text"
            placeholder="Kenteken"
            value={licensePlate}
            onChange={(e) => setLicensePlate(e.target.value)}
            className="w-full mb-4 p-2 border rounded"
            required
          />
          <input
            type="email"
            placeholder="Eigenaar email"
            value={ownerEmail}
            onChange={(e) => setOwnerEmail(e.target.value)}
            className="w-full mb-6 p-2 border rounded"
            required
          />

          <button
            type="submit"
            className="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700"
          >
            Auto toevoegen
          </button>
        </form>
      </main>
    </div>
  );
}
