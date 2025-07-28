// src/services/CarService.ts

import type { Car } from "../Types";

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/cars`;

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

export const addCar = async (car: Omit<Car, "id" | "rentals" | "rents">): Promise<Car> => {
  const res = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
    body: JSON.stringify(car),
  });

  if (!res.ok) throw new Error("Fout bij toevoegen van auto");
  return await res.json();
};
