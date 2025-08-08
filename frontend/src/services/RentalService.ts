// Import de types uit het centrale Types bestand
import { Rental } from '../Types';

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/rentals`;

// ✅ Interface die EXACT matched met RentalCreateDTO in je backend
interface RentalCreatePayload {
  carId: number;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  // PickupPoint fields (flat structure - geen nested object)
  street: string;
  number: string;
  postal: string;
  city: string;
  // Contact fields (flat structure met backend namen)
  contactName: string;  // Backend verwacht dit
  phone: string;        // Backend gebruikt 'phone' niet 'phoneNumber'
  email: string;
  ownerEmail: string;
}

// ✅ Interface voor frontend form data
interface RentalFormData {
  carId: string;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  pickupStreet: string;
  pickupNumber: string;
  pickupPostal: string;
  pickupCity: string;
  contactName: string;
  contactPhoneNumber: string;
  contactEmail: string;
  ownerEmail: string;
}

// Helper functie om email uit token te halen
const getEmailFromToken = (token: string): string | null => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.email || payload.sub || null;
  } catch (error) {
    return null;
  }
};

// Helper functie om te controleren of user ingelogd is
const checkAuth = (): { token: string; email: string } => {
  const token = localStorage.getItem("token");
  if (!token) throw new Error("Geen gebruiker ingelogd");
   
  const email = getEmailFromToken(token);
  if (!email) throw new Error("Ongeldig token");
   
  return { token, email };
};

export const getAllRentals = async (): Promise<Rental[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error("Fout bij ophalen van rentals");
  return response.json();
};

export const getRentalById = async (id: number): Promise<Rental> => {
  const response = await fetch(`${API_URL}/${id}`);
  if (!response.ok) throw new Error("Rental niet gevonden");
  return response.json();
};

// ✅ HOOFDFUNCTIE - Transform frontend data naar backend format
export const addRental = async (formData: RentalFormData): Promise<Rental> => {
  const { token, email } = checkAuth();

  // Validatie
  const carId = parseInt(formData.carId);
  if (!carId || carId <= 0) {
    throw new Error("Geldige auto ID is vereist");
  }

  // ✅ Transform frontend data naar backend DTO format
  const backendPayload: RentalCreatePayload = {
    carId: carId,                                    // ✅ Integer, niet object
    startDate: formData.startDate,
    startTime: formData.startTime,
    endDate: formData.endDate,
    endTime: formData.endTime,
    // ✅ PickupPoint fields (flatten de nested structure)
    street: formData.pickupStreet,
    number: formData.pickupNumber,
    postal: formData.pickupPostal,
    city: formData.pickupCity,
    // ✅ Contact fields (map naar backend namen)
    contactName: formData.contactName,               // ✅ Backend verwacht dit
    phone: formData.contactPhoneNumber,              // ✅ 'phone' niet 'phoneNumber'
    email: formData.contactEmail,
    ownerEmail: email,                               // ✅ Gebruik email uit token
  };

  console.log("🚀 Backend payload:", JSON.stringify(backendPayload, null, 2));

  const response = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(backendPayload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("❌ Backend error response:", errorText);
    
    try {
      const errorJson = JSON.parse(errorText);
      throw new Error(errorJson.message || "Rental kon niet toegevoegd worden");
    } catch {
      throw new Error(`HTTP ${response.status}: Rental kon niet toegevoegd worden`);
    }
  }

  return await response.json();
};

export const deleteRental = async (id: number): Promise<void> => {
  const { token } = checkAuth();
  
  const response = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error("Fout bij verwijderen van rental");
};