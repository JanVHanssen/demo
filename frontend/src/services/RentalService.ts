export interface Rental {
  id?: number;
  car: { id: number };
  startDate: string;
  endDate: string;
  city: string;
  ownerEmail: string;
}

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/rentals`;

export const getAllRentals = async (): Promise<Rental[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error("Fout bij ophalen van rentals");
  return await response.json();
};

export const getRentalById = async (id: number): Promise<Rental> => {
  const response = await fetch(`${API_URL}/${id}`);
  if (!response.ok) throw new Error("Rental niet gevonden");
  return await response.json();
};

export const addRental = async (rental: Rental): Promise<Rental> => {
  const response = await fetch(API_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(rental),
  });

  if (!response.ok) throw new Error("Rental kon niet toegevoegd worden");
  return await response.json();
};

export const deleteRental = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) throw new Error("Fout bij verwijderen van rental");
};
