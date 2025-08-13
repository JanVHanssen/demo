// AuthService.ts - Updated with complete role support

export type UserRole = 'OWNER' | 'RENTER' | 'ACCOUNTANT' | 'ADMIN';

export interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  email: string;
  roles: UserRole[];
}

export interface User {
  userId: string;
  username: string;
  email: string;
  roles: UserRole[];
}

export async function registerUser(
  username: string, 
  email: string, 
  password: string, 
  role: UserRole
): Promise<string> {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password, role }),
  });

  if (!response.ok) {
    const data = await response.json();
    throw new Error(data.error || "Registratie mislukt");
  }

  const result = await response.json();
  return result.message || "Registratie succesvol!";
}

export async function loginUser(username: string, password: string): Promise<LoginResponse> {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    const data = await response.json();
    throw new Error(data.error || "Login mislukt");
  }

  const data: LoginResponse = await response.json();

  // Token en user info opslaan in localStorage
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify({
    userId: data.userId,
    username: data.username,
    email: data.email,
    roles: data.roles
  }));

  return data;
}

export async function validateToken(): Promise<boolean> {
  const token = getToken();
  if (!token) return false;

  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/validate`, {
      method: "POST",
      headers: { 
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      },
    });

    if (!response.ok) {
      logout();
      return false;
    }

    const data = await response.json();
    return data.valid === true;
  } catch (error) {
    logout();
    return false;
  }
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

export function getCurrentUser(): User | null {
  if (typeof window === 'undefined') return null;
  
  const userStr = localStorage.getItem('user');
  if (!userStr) return null;
  
  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
}

export function hasRole(role: UserRole): boolean {
  const user = getCurrentUser();
  return user?.roles?.includes(role) || false;
}

export function isOwner(): boolean {
  return hasRole('OWNER');
}

export function isRenter(): boolean {
  return hasRole('RENTER');
}

export function isAccountant(): boolean {
  return hasRole('ACCOUNTANT');
}

export function isAdmin(): boolean {
  return hasRole('ADMIN');
}

export function hasAnyRole(roles: UserRole[]): boolean {
  const user = getCurrentUser();
  if (!user?.roles) return false;
  return roles.some(role => user.roles.includes(role));
}

export function hasAllRoles(roles: UserRole[]): boolean {
  const user = getCurrentUser();
  if (!user?.roles) return false;
  return roles.every(role => user.roles.includes(role));
}

export function canManageUsers(): boolean {
  return isAdmin();
}

export function canManageCars(): boolean {
  return isAdmin() || isOwner();
}

export function canRentCars(): boolean {
  return isRenter() || isAdmin();
}

export function canViewBookkeeping(): boolean {
  return isAccountant() || isAdmin();
}

export function logout(): void {
  if (typeof window === 'undefined') return;
  
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  
  // Redirect to login page
  window.location.href = '/login';
}

export function isAuthenticated(): boolean {
  return getToken() !== null;
}