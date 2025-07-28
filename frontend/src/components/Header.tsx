import React from "react";
import { Bell } from "lucide-react";

const Header: React.FC = () => {
  return (
    <header className="bg-blue-600 text-white p-4 flex items-center justify-between">
      {/* App title */}
      <div className="text-2xl font-bold">Car4Rental</div>

      {/* Navigation */}
      <nav className="flex gap-6 text-lg items-center">
        <a href="/" className="hover:underline">Home</a>
        <a href="/cars" className="hover:underline">Cars</a>
        <a href="/rentals" className="hover:underline">Rentals</a>
        <a href="/rents" className="hover:underline">Rents</a>
        <a href="/loginpage" className="hover:underline">Login</a>
        <a href="/registerpage" className="hover:underline">Register</a>
      </nav>

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
    </header>
  );
};

export default Header;
