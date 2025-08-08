import React, { useState, useEffect } from "react";
import { Bell, User, LogOut } from "lucide-react";

interface UserInfo {
  username: string;
  email?: string;
}

const Header: React.FC = () => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [showUserMenu, setShowUserMenu] = useState(false);

  useEffect(() => {
    // Check if user is logged in when component mounts
    checkAuthStatus();
  }, []);

 const checkAuthStatus = async () => {
  const token = localStorage.getItem('token');
  console.log("Token gevonden:", token);

  if (!token) {
    setUser(null);
    return;
  }

  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/validate`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    const data = await response.json();
    console.log("Validatie response:", data);

    if (response.ok && data.valid) {
      const username = getUsernameFromToken(token);
      console.log("Gebruikersnaam uit token:", username);
      setUser({ username });
    } else {
      localStorage.removeItem('token');
      setUser(null);
    }
  } catch (error) {
    console.error('Fout bij valideren token:', error);
    setUser(null);
  }
};


  // Simple function to decode JWT and get username (client-side only for display)
  const getUsernameFromToken = (token: string): string => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || 'Unknown';
    } catch (error) {
      return 'Unknown';
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setShowUserMenu(false);
    // Optionally redirect to home page
    window.location.href = '/';
  };

  const toggleUserMenu = () => {
    setShowUserMenu(!showUserMenu);
  };

  return (
    <header className="bg-blue-600 text-white p-4 flex items-center justify-between">
      {/* App title */}
      <div className="text-2xl font-bold">Car4Rental</div>

      {/* Navigation */}
      <nav className="flex gap-6 text-lg items-center">
        <a href="/" className="hover:underline">Home</a>
        <a href="/Cars" className="hover:underline">Cars</a>
        <a href="/Rentals" className="hover:underline">Rentals</a>
        <a href="/Rents" className="hover:underline">Rents</a>
        
        {/* Show Login/Register only if not logged in */}
        {!user && (
          <>
            <a href="/Login" className="hover:underline">Login</a>
            <a href="/Register" className="hover:underline">Register</a>
          </>
        )}
      </nav>

      {/* Right side - User info and notifications */}
      <div className="flex items-center gap-4">
        {/* User section */}
        {user ? (
          <div className="relative">
            <button
              onClick={toggleUserMenu}
              className="flex items-center gap-2 hover:text-yellow-300 bg-blue-700 px-3 py-1 rounded-md"
            >
              <User size={20} />
              <span className="font-medium">{user.username}</span>
            </button>
            
            {/* User dropdown menu */}
            {showUserMenu && (
              <div className="absolute right-0 mt-2 w-48 bg-white text-black rounded-md shadow-lg z-50">
                <div className="p-3 border-b border-gray-200">
                  <div className="font-medium text-gray-900">{user.username}</div>
                  <div className="text-sm text-gray-500">Ingelogd</div>
                </div>
                <div className="py-1">
                  <a 
                    href="/profile" 
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Profiel
                  </a>
                  <a 
                    href="/settings" 
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Instellingen
                  </a>
                  <button
                    onClick={handleLogout}
                    className="w-full text-left block px-4 py-2 text-sm text-red-600 hover:bg-gray-100 flex items-center gap-2"
                  >
                    <LogOut size={16} />
                    Uitloggen
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          // Show login button when not logged in
          <a 
            href="/Login" 
            className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 px-3 py-1 rounded-md"
          >
            <User size={20} />
            <span>Inloggen</span>
          </a>
        )}

        {/* Notification icon */}
        <div className="relative">
          <button
            className="hover:text-yellow-300"
            aria-label="Notifications"
          >
            <Bell size={24} />
          </button>
          {/* Notification count badge */}
          <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full px-1">
            3
          </span>
        </div>
      </div>
    </header>
  );
};

export default Header;