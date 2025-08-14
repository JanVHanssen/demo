import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import Header from "../../../components/Header";
import { CarType } from "../../../Types";
import { updateCar, fetchCarById } from "../../../services/CarService";
import type { Car } from "../../../Types";
import { GetStaticProps } from "next";
import { serverSideTranslations } from "next-i18next/serverSideTranslations";

export default function EditCarPage() {
  const router = useRouter();
  const { id } = router.query;

  const [car, setCar] = useState<Omit<Car, "id" | "ownerEmail"> | null>(null);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [message, setMessage] = useState("");
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const checkAuthStatus = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      setIsAuthenticated(false);
      setIsLoading(false);
      router.push("/login");
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/validate`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      const data = await response.json();

      if (response.ok && data.valid) {
        setIsAuthenticated(true);
      } else {
        localStorage.removeItem("token");
        setIsAuthenticated(false);
        router.push("/login");
      }
    } catch (error) {
      console.error("Fout bij valideren token:", error);
      setIsAuthenticated(false);
      router.push("/login");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  useEffect(() => {
    const fetchCar = async () => {
      if (!id) return;

      try {
        const found = await fetchCarById(Number(id));

        // ✅ FIXED: Remove only ownerEmail, no rentals property
        const { ownerEmail, ...carData } = found;

        setCar(carData);
      } catch (error) {
        console.error("Fout bij ophalen auto:", error);
        router.push("/cars");
      }
    };

    fetchCar();
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage("");
    setErrors({});

    try {
      if (!id || !car) return;
      await updateCar(Number(id), car);
      setMessage("Auto succesvol bijgewerkt!");
      setTimeout(() => router.push("/cars"), 2000);
    } catch (err: any) {
      if (err.response && err.response.data) {
        setErrors(err.response.data);
      } else {
        setErrors({ general: err.message || "Onbekende fout" });
        if (err.message.includes("token")) router.push("/login");
      }
    }
  };

  if (isLoading || !car) {
    return <div className="min-h-screen flex items-center justify-center">Laden...</div>;
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">
      <main className="flex-grow flex items-center justify-center">
        <form onSubmit={handleSubmit} className="bg-white p-8 rounded shadow-md w-full max-w-md">
          <h2 className="text-2xl font-bold mb-6 text-center">Bewerk auto</h2>

          {message && <p className="text-green-600 mb-4">{message}</p>}
          {errors.general && <p className="text-red-500 mb-4">{errors.general}</p>}

          {/* Merk */}
          <input
            type="text"
            placeholder="Merk"
            value={car.brand}
            onChange={(e) => setCar({ ...car, brand: e.target.value })}
            className="w-full mb-1 p-2 border rounded"
            required
          />
          {errors.brand && <p className="text-red-500 text-sm mb-2">{errors.brand}</p>}

          {/* Model */}
          <input
            type="text"
            placeholder="Model"
            value={car.model}
            onChange={(e) => setCar({ ...car, model: e.target.value })}
            className="w-full mb-1 p-2 border rounded"
            required
          />
          {errors.model && <p className="text-red-500 text-sm mb-2">{errors.model}</p>}

          {/* Kenteken */}
          <input
            type="text"
            placeholder="Kenteken"
            value={car.licensePlate}
            onChange={(e) => setCar({ ...car, licensePlate: e.target.value.toUpperCase() })}
            className="w-full mb-1 p-2 border rounded"
            required
          />
          {errors.licensePlate && <p className="text-red-500 text-sm mb-2">{errors.licensePlate}</p>}

          {/* Type auto */}
          <label className="block mb-1">Type auto</label>
          <select
            value={car.type}
            onChange={(e) => setCar({ ...car, type: e.target.value as CarType })}
            className="w-full mb-1 p-2 border rounded"
          >
            {Object.values(CarType).map((t) => (
              <option key={t} value={t}>
                {t.replace("_", " ")}
              </option>
            ))}
          </select>

          {/* Zitplaatsen */}
          <label className="block mb-1">Aantal zitplaatsen</label>
          <select
            value={car.numberOfSeats}
            onChange={(e) => setCar({ ...car, numberOfSeats: parseInt(e.target.value) })}
            className="w-full mb-1 p-2 border rounded"
          >
            {Array.from({ length: 9 }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>

          {/* Kinderzitjes */}
          <label className="block mb-1">Aantal kinderzitjes</label>
          <select
            value={car.numberOfChildSeats}
            onChange={(e) => setCar({ ...car, numberOfChildSeats: parseInt(e.target.value) })}
            className="w-full mb-4 p-2 border rounded"
          >
            {Array.from({ length: car.numberOfSeats + 1 }, (_, i) => i).map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>

          {/* ✅ ADDED: Price per day */}
          <label className="block mb-1">Prijs per dag (€)</label>
          <input
            type="number"
            placeholder="Prijs per dag"
            value={car.pricePerDay}
            onChange={(e) => setCar({ ...car, pricePerDay: parseFloat(e.target.value) || 0 })}
            className="w-full mb-4 p-2 border rounded"
            min="0"
            step="0.01"
            required
          />
          {errors.pricePerDay && <p className="text-red-500 text-sm mb-2">{errors.pricePerDay}</p>}

          {/* Checkboxen */}
          <label className="inline-flex items-center mb-2">
            <input
              type="checkbox"
              checked={car.foldingRearSeat}
              onChange={(e) => setCar({ ...car, foldingRearSeat: e.target.checked })}
              className="mr-2"
            />
            Inklapbare achterbank
          </label>

          {/* ✅ FIXED: towBar → towbar */}
          <label className="inline-flex items-center mb-2">
            <input
              type="checkbox"
              checked={car.towbar}
              onChange={(e) => setCar({ ...car, towbar: e.target.checked })}
              className="mr-2"
            />
            Trekhaak
          </label>

          {/* ✅ FIXED: availableForRent → available */}
          <label className="inline-flex items-center mb-6">
            <input
              type="checkbox"
              checked={car.available}
              onChange={(e) => setCar({ ...car, available: e.target.checked })}
              className="mr-2"
            />
            Beschikbaar voor verhuur
          </label>

          <button type="submit" className="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700">
            Wijzigingen opslaan
          </button>
        </form>
      </main>
    </div>
  );
}

