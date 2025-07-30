const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/auth`;

export interface AuthResponse {
  token: string;
}

export const login = async (
  username: string,
  password: string
): Promise<AuthResponse> => {
  const response = await fetch(`${API_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error("Login mislukt");
  return await response.json();
};

export const register = async (
  username: string,
  email: string,
  password: string
): Promise<string> => {
  const response = await fetch(`${API_URL}/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }),
  });

  if (!response.ok) throw new Error("Registratie mislukt");
  return await response.text();
};
