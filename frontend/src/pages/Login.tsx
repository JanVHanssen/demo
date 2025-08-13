import { useState, useEffect } from "react";
import { useRouter } from "next/router";
import Header from "../components/Header";
import { loginUser } from "../services/AuthService";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  
  const router = useRouter();

  useEffect(() => {
    // Check for success message from registration
    if (router.query.message) {
      setSuccessMessage(router.query.message as string);
    }
  }, [router.query]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const loginResponse = await loginUser(username, password);
      
      // Show success message with user info
      console.log('Login successful:', {
        user: loginResponse.username,
        email: loginResponse.email,
        roles: loginResponse.roles
      });
      
      // Redirect based on user role or to dashboard
      const userRoles = loginResponse.roles;
      
      // Priority-based redirect (Admin has highest priority)
      if (userRoles.includes('ADMIN')) {
        router.push('/dashboard/admin');
      } else if (userRoles.includes('ACCOUNTANT')) {
        router.push('/dashboard/accountant');
      } else if (userRoles.includes('OWNER')) {
        router.push('/dashboard/owner');
      } else if (userRoles.includes('RENTER')) {
        router.push('/dashboard/renter');
      } else {
        router.push('/dashboard');
      }
      
    } catch (err: any) {
      setError(err.message || "Login mislukt");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">
      <Header />

      <main className="flex-grow flex items-center justify-center px-4">
        <form
          onSubmit={handleSubmit}
          className="bg-white p-8 rounded-lg shadow-md w-full max-w-md"
        >
          <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">
            Inloggen
          </h2>
          
          {successMessage && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
              {successMessage}
            </div>
          )}
          
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <div className="mb-4">
            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
              Gebruikersnaam
            </label>
            <input
              id="username"
              type="text"
              placeholder="Voer je gebruikersnaam in"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>

          <div className="mb-6">
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
              Wachtwoord
            </label>
            <input
              id="password"
              type="password"
              placeholder="Voer je wachtwoord in"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className={`w-full p-3 rounded-md text-white font-medium transition-colors ${
              isLoading
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-700 focus:ring-2 focus:ring-blue-500'
            }`}
          >
            {isLoading ? (
              <span className="flex items-center justify-center">
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Inloggen...
              </span>
            ) : (
              'Inloggen'
            )}
          </button>

          <div className="text-center mt-6">
            <p className="text-sm text-gray-600">
              Nog geen account?{' '}
              <button
                type="button"
                onClick={() => router.push('/register')}
                className="text-blue-600 hover:text-blue-700 font-medium"
              >
                Registreer hier
              </button>
            </p>
          </div>

          <div className="text-center mt-4">
            <p className="text-xs text-gray-500">
              Admin login: admin@car4rent.com / admin123
            </p>
          </div>
        </form>
      </main>
    </div>
  );
}