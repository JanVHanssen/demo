// src/services/RentService.ts

export interface Rent {
  id?: number;
  car: { id: number };
  startDate: string;
  endDate: string;
  ownerEmail: string;
  renterEmail: string;
}

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/rents`;

export const getAllRents = async (): Promise<Rent[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error("Fout bij ophalen van rents");
  return await response.json();
};

export const getRentById = async (id: number): Promise<Rent> => {
  const response = await fetch(`${API_URL}/${id}`);
  if (!response.ok) throw new Error("Rent niet gevonden");
  return await response.json();
};

export const addRent = async (rent: Rent): Promise<Rent> => {
  const response = await fetch(API_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(rent),
  });

  if (!response.ok) throw new Error("Rent kon niet toegevoegd worden");
  return await response.json();
};

export const deleteRent = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) throw new Error("Fout bij verwijderen van rent");
};
