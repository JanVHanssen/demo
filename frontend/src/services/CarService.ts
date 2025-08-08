// src/services/CarService.ts
import type { Car } from "../Types";

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/cars`;

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

// Haal alle auto's van de ingelogde user op
export const fetchUserCars = async (): Promise<Car[]> => {
  const { token, email } = checkAuth();
  
  const res = await fetch(`${API_URL}/owner/${encodeURIComponent(email)}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error("Fout bij ophalen van auto's");
  return await res.json();
};

// Behoud de originele fetchCars voor admin gebruik
export const fetchCars = async (): Promise<Car[]> => {
  const res = await fetch(API_URL, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  if (!res.ok) throw new Error("Fout bij ophalen van auto's");
  return await res.json();
};

export const deleteCar = async (id: number): Promise<void> => {
  const res = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  if (!res.ok) throw new Error("Fout bij verwijderen van auto");
};

export const addCar = async (car: Omit<Car, "id" | "rentals" | "rents" | "ownerEmail">): Promise<Car> => {
  const { token, email } = checkAuth();
  
  // Voeg automatisch de ingelogde user toe als owner
  const carWithOwner = {
    ...car,
    ownerEmail: email
  };

  const res = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(carWithOwner),
  });

  if (!res.ok) {
    // Als het een 400 error is (validatie), probeer de validatie errors te parsen
    if (res.status === 400) {
      const errorData = await res.json().catch(() => ({}));
      const error = new Error("Validatie fout");
      (error as any).response = { data: errorData };
      throw error;
    }
    throw new Error("Fout bij toevoegen van auto");
  }
  return await res.json();
};

export const updateCar = async (
  id: number,
  car: Omit<Car, "id" | "rentals" | "rents" | "ownerEmail">
): Promise<Car> => {
  const token = localStorage.getItem("token");
  if (!token) throw new Error("Geen gebruiker ingelogd");

  const email = getEmailFromToken(token);
  if (!email) throw new Error("Ongeldig token");

  const carWithOwner = {
    ...car,
    ownerEmail: email,
  };

  const res = await fetch(`${API_URL}/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(carWithOwner),
  });

  if (!res.ok) {
    if (res.status === 400) {
      const errorData = await res.json().catch(() => ({}));
      const error = new Error("Validatie fout");
      (error as any).response = { data: errorData };
      throw error;
    }
    throw new Error("Fout bij updaten van auto");
  }

  return await res.json();
};

// Extra service methods voor de nieuwe endpoints
export const fetchAvailableCars = async (): Promise<Car[]> => {
  const res = await fetch(`${API_URL}/available`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  if (!res.ok) throw new Error("Fout bij ophalen van beschikbare auto's");
  return await res.json();
};

export const fetchCarsByOwner = async (email: string): Promise<Car[]> => {
  const res = await fetch(`${API_URL}/owner/${encodeURIComponent(email)}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  if (!res.ok) throw new Error("Fout bij ophalen van auto's van eigenaar");
  return await res.json();
};

export const fetchCarsByType = async (type: string): Promise<Car[]> => {
  const res = await fetch(`${API_URL}/type/${type}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  if (!res.ok) throw new Error("Fout bij ophalen van auto's per type");
  return await res.json();
};

export const fetchCarByLicensePlate = async (licensePlate: string): Promise<Car> => {
  const res = await fetch(`${API_URL}/license/${encodeURIComponent(licensePlate)}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  if (!res.ok) {
    if (res.status === 404) {
      throw new Error("Auto niet gevonden");
    }
    throw new Error("Fout bij ophalen van auto");
  }
  return await res.json();
};

export const fetchCarById = async (id: number): Promise<Car> => {
  const res = await fetch(`${API_URL}/${id}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });

  if (!res.ok) {
    if (res.status === 404) {
      throw new Error("Auto niet gevonden");
    }
    throw new Error("Fout bij ophalen van auto");
  }

  return await res.json();
};
