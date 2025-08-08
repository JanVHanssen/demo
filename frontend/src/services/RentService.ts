
import type { Car, Rent } from "../Types";
import { fetchCars } from "./CarService"; 

const getEmailFromToken = (token: string): string | null => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.email || payload.sub || null;
  } catch (error) {
    return null;
  }
};

const checkAuth = (): { token: string; email: string } => {
  const token = localStorage.getItem("token");
  if (!token) throw new Error("Geen gebruiker ingelogd");
  
  const email = getEmailFromToken(token);
  if (!email) throw new Error("Ongeldig token");
  
  return { token, email };
};

interface RentCreatePayload {
  carId: number;                    
  startDate: string;
  endDate: string;
  ownerEmail: string;
  renterEmail: string;              
  phoneNumber: string;              
  nationalRegisterId: string;
  birthDate: string;                
  drivingLicenseNumber: string;    
}

interface RentFormData {
  carId: number;
  startDate: string;
  endDate: string;
  ownerEmail: string;
  renterEmail: string;
  phoneNumber: string;
  nationalRegisterId: string;
  birthDate: string;
  drivingLicenseNumber: string;
}

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/rents`;

export const fetchUserRents = async (): Promise<Rent[]> => {
  const { token, email } = checkAuth();

  console.log("🔍 JWT Token email:", email);
  console.log("🔍 Zoeken naar rents voor:", email);

  const res = await fetch(`${API_URL}/renter/${encodeURIComponent(email)}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    console.error("❌ Response not OK:", res.status, res.statusText);
    throw new Error("Fout bij ophalen van je huurgeschiedenis");
  }

  const data = await res.json();
  console.log("📥 Response data:", data);
  console.log("📊 Number of rents found:", data.length);

  return data;
};

export const getAllRents = async (): Promise<Rent[]> => {
  const token = localStorage.getItem("token");
  
  const headers: HeadersInit = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(API_URL, {
      headers
    });
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: Fout bij ophalen van rents`);
    }
    
    const data = await response.json();
    console.log("Raw rents data from backend:", data);
    
    return data;
  } catch (error) {
    console.error("Error in getAllRents:", error);
    throw new Error("Fout bij ophalen van rents");
  }
};

export const getRentById = async (id: number): Promise<Rent> => {
  const token = localStorage.getItem("token");
  const headers: HeadersInit = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_URL}/${id}`, { headers });
  if (!response.ok) throw new Error("Rent niet gevonden");
  return await response.json();
};

export const addRent = async (formData: RentFormData): Promise<Rent> => {
  const { token, email } = checkAuth();
  
  console.log("📥 Incoming formData:", formData);
  
  // Validatie
  if (!formData.carId || formData.carId <= 0) {
    throw new Error("Geldige auto ID is vereist");
  }
  
  if (!formData.ownerEmail) {
    throw new Error("Owner email is vereist");
  }
  
  if (!formData.renterEmail) {
    throw new Error("Renter email is vereist");
  }

  const payload: RentCreatePayload = {
    carId: Number(formData.carId),
    startDate: formData.startDate,
    endDate: formData.endDate,
    ownerEmail: formData.ownerEmail.trim(),
    renterEmail: formData.renterEmail.trim(),    
    phoneNumber: formData.phoneNumber.trim(),   
    nationalRegisterId: formData.nationalRegisterId.trim(),
    birthDate: formData.birthDate,               
    drivingLicenseNumber: formData.drivingLicenseNumber.trim(),
  };

  const requiredFields = ['carId', 'startDate', 'endDate', 'ownerEmail', 'renterEmail', 'phoneNumber'];
  for (const field of requiredFields) {
    if (!payload[field as keyof RentCreatePayload]) {
      throw new Error(`Required field missing: ${field}`);
    }
  }

  console.log("🚀 Final payload:", JSON.stringify(payload, null, 2));
  console.log("🔍 CarId type:", typeof payload.carId, "Value:", payload.carId);

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };

  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers,
      body: JSON.stringify(payload),
    });

    console.log("📡 Response status:", response.status);
    console.log("📡 Response headers:", Object.fromEntries(response.headers.entries()));

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ Backend error response:", errorText);
      
      try {
        const errorJson = JSON.parse(errorText);
        console.error("❌ Parsed error:", errorJson);
        throw new Error(errorJson.message || `HTTP ${response.status}: ${errorText}`);
      } catch (parseError) {
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }
    }

    const result = await response.json();
    console.log("✅ Success response:", result);
    return result;

  } catch (error) {
    console.error("💥 Fetch error:", error);
    throw error;
  }
};

export const deleteRent = async (id: number): Promise<void> => {
  const { token } = checkAuth();
  
  const response = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error("Fout bij verwijderen van rent");
};

export const fetchUserRentsWithCarDetails = async (): Promise<Rent[]> => {
  const { token, email } = checkAuth();

  const rentsResponse = await fetch(`${API_URL}/renter/${encodeURIComponent(email)}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!rentsResponse.ok) {
    throw new Error("Fout bij ophalen van je huurgeschiedenis");
  }

  const rents: Rent[] = await rentsResponse.json();
  
  try {
    const cars = await fetchCars();
    
    const rentsWithCarDetails = rents.map(rent => {
      const car = cars.find(c => c.id === rent.carId);
      return {
        ...rent,
        car: car
      };
    });
    
    return rentsWithCarDetails;
  } catch (error) {
    console.warn("Kon auto details niet ophalen, toon alleen carId:", error);
    return rents;
  }
};

export const debugAllRents = async (): Promise<void> => {
  try {
    const allRents = await getAllRents();
    console.log("🗄️ ALL RENTS IN DATABASE:");
    allRents.forEach((rent, index) => {
      console.log(`Rent ${index + 1}:`, {
        id: rent.id,
        carId: rent.carId,
        renterEmail: rent.renterEmail,
        ownerEmail: rent.ownerEmail
      });
    });
  } catch (error) {
    console.error("❌ Could not fetch all rents for debug:", error);
  }
};