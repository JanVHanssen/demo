import { useEffect, useState } from "react";
import { getAllRentals, deleteRental } from "@/services/RentalService";
import Link from "next/link";
import { Rental } from "@/Types";
import { useRouter } from "next/router";

export default function RentalsPage() {
  const [rentals, setRentals] = useState<Rental[]>([]);
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

  const loadRentals = async () => {
    try {
      const data = await getAllRentals();
      setRentals(data);
    } catch (err) {
      setError("Could not load rentals");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRental(id);
      setRentals((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      alert("Delete failed");
    }
  };

  useEffect(() => {
    loadRentals();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <main className="flex-grow p-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Rentals</h1>
          <Link href="/AddRental" passHref>
            <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
              Add rental
            </button>
          </Link>
        </div>

        {error && <p className="text-red-500 mb-4">{error}</p>}

        {rentals.length === 0 ? (
          <p className="text-gray-600 text-lg">No rentals yet</p>
        ) : (
          <ul className="space-y-6">
            {rentals.map((rental) => (
              <li
                key={rental.id}
                className="bg-white p-6 rounded shadow flex flex-col sm:flex-row sm:justify-between sm:items-center"
              >
                <div>
                  <p className="text-xl font-semibold">
                    Auto: {rental.car?.brand} {rental.car?.model}
                  </p>
                  <p>
                    Period: {rental.startDate} {rental.startTime} -{" "}
                    {rental.endDate} {rental.endTime}
                  </p>
                  <p>
                    Pickup point: {rental.pickupPoint?.street}{" "}
                    {rental.pickupPoint?.number}, {rental.pickupPoint?.postal}{" "}
                    {rental.pickupPoint?.city}
                  </p>
                  <p>
                    Contact: {rental.contact?.phoneNumber} |{" "}
                    {rental.contact?.email}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">
                    Owner: {rental.ownerEmail}
                  </p>
                </div>

                <div className="flex flex-col sm:items-end gap-2 mt-4 sm:mt-0">
                  <button
                    onClick={() => rental.id && handleDelete(rental.id)}
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
