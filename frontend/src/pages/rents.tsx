import { useEffect, useState, FormEvent, ChangeEvent } from "react";
import {
  getAllRents,
  addRent,
  deleteRent,
  Rent,
} from "@/services/RentService";

const RentsPage = () => {
  const [rents, setRents] = useState<Rent[]>([]);
  const [form, setForm] = useState({
    carId: "",
    startDate: "",
    endDate: "",
    ownerEmail: "",
    renterEmail: "",
  });

  useEffect(() => {
    fetchRents();
  }, []);

  const fetchRents = async () => {
    try {
      const data = await getAllRents();
      setRents(data);
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

    const newRent: Rent = {
      car: { id: Number(form.carId) },
      startDate: form.startDate,
      endDate: form.endDate,
      ownerEmail: form.ownerEmail,
      renterEmail: form.renterEmail,
    };

    try {
      await addRent(newRent);
      setForm({
        carId: "",
        startDate: "",
        endDate: "",
        ownerEmail: "",
        renterEmail: "",
      });
      fetchRents();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRent(id);
      fetchRents();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Rents</h1>

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
          type="email"
          name="ownerEmail"
          placeholder="Owner Email"
          value={form.ownerEmail}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        <input
          type="email"
          name="renterEmail"
          placeholder="Renter Email"
          value={form.renterEmail}
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
        {rents.map((rent) => (
          <li
            key={rent.id}
            className="border p-4 rounded flex justify-between items-center"
          >
            <div>
              <p><strong>Auto ID:</strong> {rent.car.id}</p>
              <p><strong>Periode:</strong> {rent.startDate} - {rent.endDate}</p>
              <p><strong>Eigenaar:</strong> {rent.ownerEmail}</p>
              <p><strong>Huurder:</strong> {rent.renterEmail}</p>
            </div>
            <button
              onClick={() => rent.id && handleDelete(rent.id)}
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

export default RentsPage;
