import { useState, useEffect, ChangeEvent, FormEvent } from "react";
import { useRouter } from "next/router";
import { addRent } from "@/services/RentService";
import { getAllRentals } from "@/services/RentalService";
import { Rental } from "@/Types";

const AddRentPage = () => {
  const router = useRouter();
  const [rentals, setRentals] = useState<Rental[]>([]);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const [form, setForm] = useState({
    rentalId: "",           
    renterEmail: "",        
    phoneNumber: "",
    nationalRegisterId: "",
    birthDate: "",
    drivingLicenseNumber: "",
  });

  // ✅ Helper functie om email uit JWT te halen
  const getEmailFromToken = (token: string): string | null => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.email || payload.sub || null;
    } catch (error) {
      return null;
    }
  };

  // Controleer of user is ingelogd met server validatie
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
        
        // ✅ Automatisch renterEmail invullen met ingelogde gebruiker
        const email = getEmailFromToken(token);
        if (email) {
          setForm(prev => ({ ...prev, renterEmail: email }));
        }
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

  useEffect(() => {
    if (isAuthenticated) {
      const loadRentals = async () => {
        try {
          const data = await getAllRentals();
          setRentals(data);
        } catch (error) {
          console.error("Fout bij ophalen van rentals:", error);
          setError("Kon geen beschikbare auto's laden");
        }
      };

      loadRentals();
    }
  }, [isAuthenticated]);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError(""); 
  };



const handleSubmit = async (e: FormEvent) => {
  e.preventDefault();
  setError("");
  setIsSubmitting(true);

  try {
    // ✅ Verbeterde validatie
    if (!form.rentalId) {
      setError("Selecteer een rental");
      return;
    }

    const rentalId = parseInt(form.rentalId);
    if (isNaN(rentalId) || rentalId <= 0) {
      setError("Ongeldige rental geselecteerd");
      return;
    }

    // ✅ Vind de geselecteerde rental
    const selectedRental = rentals.find(rental => rental.id === rentalId);
    if (!selectedRental) {
      setError("Geselecteerde rental niet gevonden");
      return;
    }

    // ✅ DEBUG: Check wat er in selectedRental staat
    console.log("🔍 Selected rental object:", selectedRental);
    console.log("🔍 Selected rental ownerEmail:", selectedRental.ownerEmail);
    console.log("🔍 Selected rental contact:", selectedRental.contact);

    // ✅ Valideer dat selectedRental.car.id bestaat
    if (!selectedRental.car?.id) {
      setError("Geen geldige auto gekoppeld aan deze rental");
      return;
    }

    let ownerEmailToUse = selectedRental.ownerEmail;
    
    // ✅ Check of ownerEmail wel een email is (bevat @)
    if (!ownerEmailToUse.includes('@')) {
      console.warn("⚠️  ownerEmail bevat geen @, probeer contact.email:", selectedRental.contact?.email);
      
      // Probeer contact.email als backup
      if (selectedRental.contact?.email) {
        ownerEmailToUse = selectedRental.contact.email;
        console.log("✅ Gebruik contact.email als ownerEmail:", ownerEmailToUse);
      } else {
        setError("Geen geldige owner email gevonden - noch in ownerEmail noch in contact.email");
        return;
      }
    }

    const rentData = {
      carId: Number(selectedRental.car.id),    
      startDate: selectedRental.startDate,     
      endDate: selectedRental.endDate,         
      ownerEmail: ownerEmailToUse,            
      renterEmail: form.renterEmail.trim(),    
      phoneNumber: form.phoneNumber.trim(),
      nationalRegisterId: form.nationalRegisterId.trim(),
      birthDate: form.birthDate,
      drivingLicenseNumber: form.drivingLicenseNumber.trim(),
    };

    // ✅ Extra validatie voor rentData
    console.log("🔍 Final rentData:", rentData);
    console.log("🔍 ownerEmail final:", rentData.ownerEmail);
    
    if (!rentData.carId || rentData.carId <= 0) {
      setError("Ongeldige auto ID");
      return;
    }

    if (!rentData.ownerEmail || !rentData.ownerEmail.includes('@')) {
      setError("Geen geldige eigenaar email gevonden");
      return;
    }

    await addRent(rentData);
    
    alert("Rent succesvol toegevoegd!");
    router.push("/rents");

  } catch (err: any) {
    console.error("❌ Submit error:", err);
    setError(err.message || "Fout bij toevoegen van rent");
  } finally {
    setIsSubmitting(false);
  }
};

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Laden...</p>
      </div>
    );
  }

  if (!isAuthenticated) return null;

  if (rentals.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600 text-lg mb-4">Er zijn geen beschikbare auto's om te huren.</p>
          <button 
            onClick={() => router.push("/rentals")}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            Bekijk alle rentals
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Nieuwe Rent toevoegen</h1>
      
      {/* ✅ Error display */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Selecteer een rental
          </label>
          <select
            name="rentalId"
            value={form.rentalId}
            onChange={handleChange}
            required
            className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Kies een rental...</option>
            {rentals.map((rental) => (
              <option key={rental.id} value={rental.id}>
                {rental.car.brand} {rental.car.model} ({rental.car.licensePlate}) 
                - {rental.startDate} tot {rental.endDate}
                - Eigenaar: {rental.ownerEmail}
              </option>
            ))}
          </select>
        </div>

        {/* ✅ Toon rental details als er een geselecteerd is */}
        {form.rentalId && (
          <div className="bg-gray-50 p-4 rounded border">
            <h3 className="font-medium text-gray-900 mb-2">Rental Details</h3>
            {(() => {
              const selected = rentals.find(r => r.id === parseInt(form.rentalId));
              return selected ? (
                <div className="text-sm text-gray-600">
                  <p><strong>Auto:</strong> {selected.car.brand} {selected.car.model}</p>
                  <p><strong>Kenteken:</strong> {selected.car.licensePlate}</p>
                  <p><strong>Periode:</strong> {selected.startDate} - {selected.endDate}</p>
                  <p><strong>Eigenaar:</strong> {selected.ownerEmail}</p>
                </div>
              ) : null;
            })()}
          </div>
        )}

        <div className="border-t pt-4">
          <h2 className="font-semibold text-gray-900 mb-4">Gegevens huurder</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input 
                type="email" 
                name="renterEmail" 
                placeholder="je@email.com" 
                value={form.renterEmail} 
                onChange={handleChange} 
                required 
                className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500" 
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Telefoonnummer
              </label>
              <input 
                type="tel" 
                name="phoneNumber" 
                placeholder="0477123456" 
                value={form.phoneNumber} 
                onChange={handleChange} 
                required 
                className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500" 
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Rijksregisternummer
              </label>
              <input 
                type="text" 
                name="nationalRegisterId" 
                placeholder="12.34.56-789.01" 
                value={form.nationalRegisterId} 
                onChange={handleChange} 
                required 
                className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500" 
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Geboortedatum
              </label>
              <input 
                type="date" 
                name="birthDate" 
                value={form.birthDate} 
                onChange={handleChange} 
                required 
                className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500" 
              />
            </div>
            
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Rijbewijsnummer
              </label>
              <input 
                type="text" 
                name="drivingLicenseNumber" 
                placeholder="1234567890" 
                value={form.drivingLicenseNumber} 
                onChange={handleChange} 
                required 
                className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500" 
              />
            </div>
          </div>
        </div>

        <div className="flex gap-4 pt-4">
          <button 
            type="submit" 
            disabled={isSubmitting}
            className={`px-6 py-2 rounded text-white font-medium ${
              isSubmitting 
                ? 'bg-gray-400 cursor-not-allowed' 
                : 'bg-blue-600 hover:bg-blue-700'
            }`}
          >
            {isSubmitting ? 'Bezig met toevoegen...' : 'Rent toevoegen'}
          </button>
          
          <button 
            type="button" 
            onClick={() => router.push("/rents")}
            className="bg-gray-500 text-white px-6 py-2 rounded hover:bg-gray-600"
          >
            Annuleren
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddRentPage;