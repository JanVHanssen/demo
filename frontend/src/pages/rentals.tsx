import { useEffect, useState, FormEvent, ChangeEvent } from "react";
import {
  getAllRentals,
  addRental,
  deleteRental,
  Rental,
} from "@/services/RentalService";

const RentalsPage = () => {
  const [rentals, setRentals] = useState<Rental[]>([]);
  const [form, setForm] = useState({
    carId: "",
    startDate: "",
    endDate: "",
    city: "",
    ownerEmail: "",
  });

  useEffect(() => {
    fetchRentals();
  }, []);

  const fetchRentals = async () => {
    try {
      const data = await getAllRentals();
      setRentals(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleChange = (
    e: ChangeEvent<HTMLInputElement>
  ) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    const newRental: Rental = {
      car: { id: Number(form.carId) },
      startDate: form.startDate,
      endDate: form.endDate,
      city: form.city,
      ownerEmail: form.ownerEmail,
    };

    try {
      await addRental(newRental);
      setForm({
        carId: "",
        startDate: "",
        endDate: "",
        city: "",
        ownerEmail: "",
      });
      fetchRentals();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRental(id);
      fetchRentals();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Rentals</h1>

      <form onSubmit={handleSubmit} className="space-y-4 mb-8">
        <input
          type="number"
          name="carId"
          placeholder="Car ID"
          value={form.carId}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        <input
          type="date"
          name="startDate"
          value={form.startDate}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        <input
          type="date"
          name="endDate"
          value={form.endDate}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        <input
          type="text"
          name="city"
          placeholder="City"
          value={form.city}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        <input
          type="email"
          name="ownerEmail"
          placeholder="Owner Email"
          value={form.ownerEmail}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        <button
          type="submit"
          className="bg-blue-600 text-white px-4 py-2 rounded"
        >
          Toevoegen
        </button>
      </form>

      <ul className="space-y-4">
        {rentals.map((rental) => (
          <li
            key={rental.id}
            className="border p-4 rounded flex justify-between items-center"
          >
            <div>
              <p><strong>Auto ID:</strong> {rental.car.id}</p>
              <p><strong>Periode:</strong> {rental.startDate} - {rental.endDate}</p>
              <p><strong>Stad:</strong> {rental.city}</p>
              <p><strong>Eigenaar:</strong> {rental.ownerEmail}</p>
            </div>
            <button
              onClick={() => rental.id && handleDelete(rental.id)}
              className="bg-red-500 text-white px-3 py-1 rounded"
            >
              Verwijder
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default RentalsPage;
