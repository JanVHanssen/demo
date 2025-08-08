import React, { useEffect, useState } from "react";
import { fetchUserRents, deleteRent } from "../services/RentService";
import Header from "../components/Header";
import Link from "next/link";
import { useRouter } from "next/router";
import { Rent } from "../Types";

export default function RentsPage() {
  const [rents, setRents] = useState<Rent[]>([]);
  const [error, setError] = useState("");
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const checkAuthStatus = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      setIsAuthenticated(false);
      setIsLoading(false);
      router.push("/Login");
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
        router.push("/Login");
      }
    } catch (error) {
      console.error("Fout bij valideren token:", error);
      setIsAuthenticated(false);
      router.push("/Login");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);


const loadRents = async () => {
  try {
    const { fetchUserRentsWithCarDetails } = await import("../services/RentService");
    const data = await fetchUserRentsWithCarDetails();
    console.log("📥 Loaded rents with car details:", data);
    setRents(data);
  } catch (err: any) {
    setError(err.message || "Kon huurdata niet laden");
    console.error("❌ Error loading rents:", err);
    
    if (err.message.includes("token") || err.message.includes("ingelogd")) {
      router.push("/Login");
    }
  }
};

  const handleDelete = async (id: number) => {
    if (!window.confirm("Weet je zeker dat je deze huur wilt verwijderen?")) {
      return;
    }

    try {
      await deleteRent(id);
      setRents((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      alert("Delete mislukt");
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      loadRents();
    }
  }, [isAuthenticated]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div>Loading...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <main className="flex-grow p-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Rents</h1>
          <Link href="/AddRent" passHref>
            <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
              New rent
            </button>
          </Link>
        </div>

        {error && <p className="text-red-500 mb-4">{error}</p>}



        {rents.length === 0 ? (
          <div className="flex justify-center items-center mt-20">
            <p className="text-gray-600 text-lg">No rents yet</p>
          </div>
        ) : (
          <ul className="space-y-6">
            {rents.map((rent) => (
              <li
                key={rent.id}
                className="bg-white p-6 rounded shadow flex flex-col sm:flex-row sm:justify-between sm:items-center"
              >
                <div>
                  <p className="text-xl font-semibold mb-1">
                    
                    {rent.car && `${rent.car.brand} ${rent.car.model} (${rent.car.licensePlate})`}
                  </p>
                  
                  <p>Periode: {rent.startDate} – {rent.endDate}</p>
                  <p className="text-sm text-gray-500 mt-1">
                    Eigenaar: {rent.ownerEmail}
                  </p>
                  
                  <p className="text-sm text-gray-500">
                    Jouw telefoonnummer: {rent.phoneNumber}
                  </p>
                  <p className="text-sm text-gray-500">
                    Jouw email: {rent.renterEmail}
                  </p>
                  <p className="text-sm text-gray-500">
                    Rijksregisternummer: {rent.nationalRegisterId}
                  </p>
                  <p className="text-sm text-gray-500">
                    Geboortedatum: {rent.birthDate}
                  </p>
                  <p className="text-sm text-gray-500">
                    Rijbewijs: {rent.drivingLicenseNumber}
                  </p>
                </div>

                <div className="flex flex-col sm:items-end gap-2 mt-4 sm:mt-0">
                  <button
                    onClick={() => handleDelete(rent.id!)}
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