export async function registerUser(username: string, email: string, password: string): Promise<string> {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }),
  });

  if (!response.ok) {
    const data = await response.text();
    throw new Error(data || "Registratie mislukt");
  }

  return "Registratie succesvol!";
}

export async function loginUser(username: string, password: string): Promise<string> {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    const data = await response.text();
    throw new Error(data || "Login mislukt");
  }

  const data = await response.json();

  // Token opslaan in localStorage
  localStorage.setItem('token', data.token);

  return data.token;
}
