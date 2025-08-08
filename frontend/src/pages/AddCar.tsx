import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import { addCar } from "../services/CarService";
import { CarType } from "../Types";
import { useRouter } from "next/router";

export default function AddCarPage() {
  const [brand, setBrand] = useState("");
  const [model, setModel] = useState("");
  const [licensePlate, setLicensePlate] = useState("");
  const [type, setType] = useState<CarType>(CarType.SEDAN);
  const [numberOfSeats, setNumberOfSeats] = useState(5);
  const [numberOfChildSeats, setNumberOfChildSeats] = useState(0);
  const [foldingRearSeat, setFoldingRearSeat] = useState(false);
  const [towBar, setTowBar] = useState(false);
  const [availableForRent, setAvailableForRent] = useState(true);
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const seatOptions = Array.from({ length: 9 }, (_, i) => i + 1);

  // Controleer of user is ingelogd met server validatie (zoals in Header)
  const checkAuthStatus = async () => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      setIsAuthenticated(false);
      setIsLoading(false);
      router.push("/Login");
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/validate`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      const data = await response.json();

      if (response.ok && data.valid) {
        setIsAuthenticated(true);
      } else {
        localStorage.removeItem('token');
        setIsAuthenticated(false);
        router.push("/Login");
      }
    } catch (error) {
      console.error('Fout bij valideren token:', error);
      setIsAuthenticated(false);
      router.push("/Login");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage("");
    setErrors({});

    // OwnerEmail wordt automatisch toegevoegd in de service
    const car = {
      brand,
      model,
      licensePlate,
      type,
      numberOfSeats,
      numberOfChildSeats,
      foldingRearSeat,
      towBar,
      availableForRent,
    };

    try {
      await addCar(car);
      setMessage("Auto succesvol toegevoegd!");
      // Reset form
      setBrand("");
      setModel("");
      setLicensePlate("");
      setType(CarType.SEDAN);
      setNumberOfSeats(5);
      setNumberOfChildSeats(0);
      setFoldingRearSeat(false);
      setTowBar(false);
      setAvailableForRent(true);
      
      // Redirect naar cars overzicht na 2 seconden
      setTimeout(() => {
        router.push("/cars");
      }, 2000);
    } catch (err: any) {
      if (err.response && err.response.data) {
        setErrors(err.response.data); // Backend-validatie errors
      } else {
        setErrors({ general: err.message || "Onbekende fout bij toevoegen van auto" });
        // Als de fout over authenticatie gaat, redirect naar login
        if (err.message.includes("ingelogd") || err.message.includes("token")) {
          router.push("/login");
        }
      }
    }
  };

  // Toon loading tijdens authenticatie check
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div>Laden...</div>
      </div>
    );
  }

  // Als niet geauthenticeerd, toon niets (redirect is al gebeurd)
  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">

      <main className="flex-grow flex items-center justify-center">
        <form
          onSubmit={handleSubmit}
          className="bg-white p-8 rounded shadow-md w-full max-w-md"
        >
          <h2 className="text-2xl font-bold mb-6 text-center">Voeg een auto toe</h2>

          {message && <p className="text-green-600 mb-4">{message}</p>}
          {errors.general && <p className="text-red-500 mb-4">{errors.general}</p>}

          <input
            type="text"
            placeholder="Merk"
            value={brand}
            onChange={(e) => setBrand(e.target.value)}
            className="w-full mb-1 p-2 border rounded"
            required
          />
          {errors.brand && <p className="text-red-500 text-sm mb-2">{errors.brand}</p>}

          <input
            type="text"
            placeholder="Model"
            value={model}
            onChange={(e) => setModel(e.target.value)}
            className="w-full mb-1 p-2 border rounded"
            required
          />
          {errors.model && <p className="text-red-500 text-sm mb-2">{errors.model}</p>}

          <input
            type="text"
            placeholder="Kenteken (bijv. 1-ABC-123)"
            value={licensePlate}
            onChange={(e) => setLicensePlate(e.target.value.toUpperCase())}
            className="w-full mb-1 p-2 border rounded"
            required
          />
          {errors.licensePlate && (
            <p className="text-red-500 text-sm mb-2">{errors.licensePlate}</p>
          )}

          <label className="block mb-1">Type auto</label>
          <select
            value={type}
            onChange={(e) => setType(e.target.value as CarType)}
            className="w-full mb-1 p-2 border rounded"
          >
            {Object.values(CarType).map((t) => (
              <option key={t} value={t}>
                {t.replace('_', ' ')}
              </option>
            ))}
          </select>
          {errors.type && <p className="text-red-500 text-sm mb-2">{errors.type}</p>}

          <label className="block mb-1">Aantal zitplaatsen</label>
          <select
            value={numberOfSeats}
            onChange={(e) => setNumberOfSeats(parseInt(e.target.value))}
            className="w-full mb-1 p-2 border rounded"
          >
            {seatOptions.map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
          {errors.numberOfSeats && (
            <p className="text-red-500 text-sm mb-2">{errors.numberOfSeats}</p>
          )}

          <label className="block mb-1">Aantal kinderzitjes</label>
          <select
            value={numberOfChildSeats}
            onChange={(e) => setNumberOfChildSeats(parseInt(e.target.value))}
            className="w-full mb-4 p-2 border rounded"
          >
            {Array.from({ length: numberOfSeats + 1 }, (_, i) => i).map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>

          <div className="mb-4">
            <label className="inline-flex items-center">
              <input
                type="checkbox"
                checked={foldingRearSeat}
                onChange={(e) => setFoldingRearSeat(e.target.checked)}
                className="mr-2"
              />
              Inklapbare achterbank
            </label>
          </div>

          <div className="mb-4">
            <label className="inline-flex items-center">
              <input
                type="checkbox"
                checked={towBar}
                onChange={(e) => setTowBar(e.target.checked)}
                className="mr-2"
              />
              Trekhaak
            </label>
          </div>

          <div className="mb-6">
            <label className="inline-flex items-center">
              <input
                type="checkbox"
                checked={availableForRent}
                onChange={(e) => setAvailableForRent(e.target.checked)}
                className="mr-2"
              />
              Beschikbaar voor verhuur
            </label>
          </div>

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