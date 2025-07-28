// src/pages/cars.tsx

import React, { useEffect, useState } from "react";
import { fetchCars, deleteCar } from "../services/CarService";
import Header from "../components/Header";
import Link from "next/link";

type Car = {
  id: number;
  brand: string;
  model: string;
  licensePlate: string;
  ownerEmail: string;
};

export default function CarsPage() {
  const [cars, setCars] = useState<Car[]>([]);
  const [error, setError] = useState("");

  const loadCars = async () => {
    try {
      const data = await fetchCars();
      const email = localStorage.getItem("email");
      setCars(data.filter((car) => car.ownerEmail === email));
    } catch (err) {
      setError("Kon auto's niet laden");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteCar(id);
      setCars((prev) => prev.filter((car) => car.id !== id));
    } catch (err) {
      alert("Verwijderen mislukt");
    }
  };

  useEffect(() => {
    loadCars();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="p-6 bg-white border-b border-gray-300 shadow">
        <Header />
      </header>

      <main className="flex-grow p-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Mijn Auto's</h1>
          <Link href="/AddCar" passHref>
            <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
              Auto toevoegen
            </button>
          </Link>
        </div>

        {error && <p className="text-red-500 mb-4">{error}</p>}
        {cars.length === 0 ? (
          <p>Geen auto's gevonden.</p>
        ) : (
          <ul className="space-y-4">
            {cars.map((car) => (
              <li
                key={car.id}
                className="bg-white p-4 rounded shadow flex justify-between items-center"
              >
                <div>
                  <p className="font-semibold">
                    {car.brand} {car.model}
                  </p>
                  <p className="text-gray-600">Kenteken: {car.licensePlate}</p>
                  <p className="text-sm text-gray-500">Eigenaar: {car.ownerEmail}</p>
                </div>
                <button
                  onClick={() => handleDelete(car.id)}
                  className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
                >
                  Verwijderen
                </button>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}