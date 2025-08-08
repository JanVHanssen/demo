import React, { useEffect, useState } from "react";
import { fetchUserCars, deleteCar } from "../services/CarService";
import Header from "../components/Header";
import Link from "next/link";
import { useRouter } from "next/router";
import { Car } from "../Types";

export default function CarsPage() {
  const [cars, setCars] = useState<Car[]>([]);
  const [error, setError] = useState("");
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

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
        localStorage.removeToken('token');
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

  const loadCars = async () => {
    try {
      const data = await fetchUserCars(); // Gebruik fetchUserCars in plaats van fetchCars
      setCars(data);
    } catch (err: any) {
      setError(err.message || "Kon auto's niet laden");
      // Als de fout over authenticatie gaat, redirect naar login
      if (err.message.includes("ingelogd") || err.message.includes("token")) {
        router.push("/Login");
      }
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Weet je zeker dat je deze auto wilt verwijderen?")) {
      return;
    }

    try {
      await deleteCar(id);
      setCars((prev) => prev.filter((car) => car.id !== id));
    } catch (err) {
      alert("Delete failed");
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      loadCars();
    }
  }, [isAuthenticated]);

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
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <main className="flex-grow p-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Cars</h1>
          <Link href="/AddCar" passHref>
            <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
              Add car
            </button>
          </Link>
        </div>

        {error && <p className="text-red-500 mb-4">{error}</p>}

        {cars.length === 0 ? (
          <div className="flex justify-center items-center mt-20">
            <p className="text-gray-600 text-lg">No cars yet</p>
          </div>
        ) : (
          <ul className="space-y-6">
            {cars.map((car) => (
              <li
                key={car.id}
                className="bg-white p-6 rounded shadow flex flex-col sm:flex-row sm:justify-between sm:items-center"
              >
                <div>
                  <p className="text-xl font-semibold">
                    {car.brand} {car.model}
                  </p>
                  <p>License plate: {car.licensePlate}</p>
                  <p>Type: {car.type}</p>
                  <p>Number of seats: {car.numberOfSeats}</p>
                  <p>Number of child seats: {car.numberOfChildSeats}</p>
                  <p>
                    Folding rear seat:{" "}
                    {car.foldingRearSeat ? "Ja" : "Nee"}
                  </p>
                  <p>Tow bar: {car.towbar ? "Ja" : "Nee"}</p>
                  <p>Available for rent: {car.available ? "Ja" : "Nee"}</p>
                  <p>Price per day: €{car.pricePerDay}</p>
                  <p className="text-sm text-gray-500 mt-1">
                    Eigenaar: {car.ownerEmail}
                  </p>
                </div>

                <div className="flex flex-col sm:items-end gap-2 mt-4 sm:mt-0">
                  {/* Edit button */}
                  <Link href={`/cars/${car.id}/EditCar`} passHref>
                    <button className="bg-yellow-500 text-white px-3 py-1 rounded hover:bg-yellow-600">
                      Edit
                    </button>
                  </Link>

                  <button
                    onClick={() => handleDelete(car.id)}
                    className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}