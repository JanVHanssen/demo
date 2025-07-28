// src/services/AuthService.js

const API_URL = "${process.env.NEXT_PUBLIC_API_URL}/auth"; 

export const login = async (username, password) => {
  const response = await fetch(`${API_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error("Login mislukt");
  return await response.json(); // bevat { token }
};

export const register = async (username, password) => {
  const response = await fetch(`${API_URL}/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error("Registratie mislukt");
  return await response.text(); // "Gebruiker geregistreerd"
};
