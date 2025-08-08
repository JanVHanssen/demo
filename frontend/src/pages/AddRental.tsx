import { useState, useEffect, ChangeEvent, FormEvent } from "react";
import { useRouter } from "next/router";
import { addRental } from "@/services/RentalService";
import { fetchCarsByOwner } from "@/services/CarService";
import { Car } from "../Types";

const AddRental = () => {
  const router = useRouter();
  const [cars, setCars] = useState<Car[]>([]);
  
  // ✅ Updated form state - voegt contactName toe
  const [form, setForm] = useState({
    carId: "",
    startDate: "",
    startTime: "",
    endDate: "",
    endTime: "",
    pickupStreet: "",
    pickupNumber: "",
    pickupPostal: "",
    pickupCity: "",
    contactName: "",        // ✅ Nieuw veld toegevoegd
    contactPhoneNumber: "",
    contactEmail: "",
    ownerEmail: "",
  });
  
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

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

  // Helper functie om email uit JWT te halen
  const getEmailFromToken = (token: string): string | null => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.email || payload.sub || null;
    } catch (error) {
      return null;
    }
  };

  // Auto's ophalen zodra je weet dat gebruiker ingelogd is
  useEffect(() => {
    if (!isAuthenticated) return;

    const fetchCars = async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) throw new Error("Geen token gevonden");

        const email = getEmailFromToken(token);
        if (!email) throw new Error("Kon geen e-mailadres uit token halen");

        const cars = await fetchCarsByOwner(email);
        setCars(cars);
      } catch (error) {
        console.error("Fout bij ophalen van auto's:", error);
      }
    };

    fetchCars();
  }, [isAuthenticated]);

  // ✅ Handle form changes
  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  // ✅ Updated handleSubmit - werkt met je backend
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Validatie
    if (!form.carId) {
      alert("Selecteer een auto");
      return;
    }

    const carId = parseInt(form.carId);
    if (isNaN(carId) || carId <= 0) {
      alert("Ongeldige auto geselecteerd");
      return;
    }

    // ✅ Updated required fields - inclusief contactName
    const requiredFields = [
      'startDate', 'startTime', 'endDate', 'endTime',
      'pickupStreet', 'pickupNumber', 'pickupPostal', 'pickupCity',
      'contactName', 'contactPhoneNumber', 'contactEmail'
    ];

    for (const field of requiredFields) {
      if (!form[field as keyof typeof form]) {
        alert(`${field} is verplicht`);
        return;
      }
    }

    // ✅ Data voorbereiden voor de service
    const formData = {
      carId: form.carId,
      startDate: form.startDate,
      startTime: form.startTime,
      endDate: form.endDate,
      endTime: form.endTime,
      pickupStreet: form.pickupStreet,
      pickupNumber: form.pickupNumber,
      pickupPostal: form.pickupPostal,
      pickupCity: form.pickupCity,
      contactName: form.contactName,
      contactPhoneNumber: form.contactPhoneNumber,
      contactEmail: form.contactEmail,
      ownerEmail: form.ownerEmail, // Dit wordt overschreven in de service
    };

    console.log("Form data to submit:", formData);
    console.log("Selected car:", cars.find(car => car.id === carId));

    try {
      await addRental(formData);
      alert("Rental succesvol toegevoegd!");
      router.push("/Rentals");
    } catch (err: any) {
      console.error("Fout bij toevoegen rental:", err);
      if (err.message && (err.message.includes("ingelogd") || err.message.includes("token"))) {
        router.push("/Login");
        return;
      }
      alert(`Fout bij toevoegen rental: ${err.message}`);
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
  if (!isAuthenticated) return null;
  
  if (cars.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Je hebt nog geen auto's toegevoegd. Voeg eerst een auto toe.</p>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-lg mx-auto">
      <h1 className="text-2xl font-bold mb-4">Nieuwe Rental toevoegen</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <select
          name="carId"
          value={form.carId}
          onChange={handleChange}
          required
          className="border p-2 w-full"
        >
          <option value="">Selecteer een auto</option>
          {cars.map((car) => (
            <option key={car.id} value={car.id}>
              {car.brand} {car.model} ({car.licensePlate})
            </option>
          ))}
        </select>

        <input 
          type="date" 
          name="startDate" 
          value={form.startDate} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="time" 
          name="startTime" 
          value={form.startTime} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="date" 
          name="endDate" 
          value={form.endDate} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="time" 
          name="endTime" 
          value={form.endTime} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />

        <h2 className="font-semibold">Ophaalpunt</h2>
        <input 
          type="text" 
          name="pickupStreet" 
          placeholder="Straat" 
          value={form.pickupStreet} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="text" 
          name="pickupNumber" 
          placeholder="Nummer" 
          value={form.pickupNumber} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="text" 
          name="pickupPostal" 
          placeholder="Postcode" 
          value={form.pickupPostal} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="text" 
          name="pickupCity" 
          placeholder="Stad" 
          value={form.pickupCity} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />

        <h2 className="font-semibold">Contactgegevens</h2>
        {/* ✅ Nieuw contactName veld */}
        <input 
          type="text" 
          name="contactName" 
          placeholder="Naam" 
          value={form.contactName} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="tel" 
          name="contactPhoneNumber" 
          placeholder="Telefoonnummer" 
          value={form.contactPhoneNumber} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="email" 
          name="contactEmail" 
          placeholder="E-mail" 
          value={form.contactEmail} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />

        <button 
          type="submit" 
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Toevoegen
        </button>
      </form>
    </div>
  );
};

export default AddRental;