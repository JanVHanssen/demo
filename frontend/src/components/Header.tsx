import React, { useState, useEffect } from "react";
import { Bell, User, LogOut, Settings, BarChart3 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/router";
import { 
  getCurrentUser, 
  isAuthenticated, 
  logout, 
  validateToken,
  isAdmin,
  isAccountant,
  isOwner,
  isRenter,
  canManageUsers,
  canManageCars,
  canViewBookkeeping 
} from "../services/AuthService";

interface UserInfo {
  userId: string;
  username: string;
  email: string;
  roles: string[];
}

const Header: React.FC = () => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const router = useRouter();

  useEffect(() => {
    // Check if user is logged in when component mounts
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    if (!isAuthenticated()) {
      setUser(null);
      return;
    }

    try {
      const isValid = await validateToken();
      if (isValid) {
        const currentUser = getCurrentUser();
        if (currentUser) {
          setUser(currentUser);
        }
      } else {
        setUser(null);
      }
    } catch (error) {
      console.error('Fout bij valideren token:', error);
      setUser(null);
    }
  };

  const handleLogout = () => {
    logout();
    setUser(null);
    setShowUserMenu(false);
  };

  const toggleUserMenu = () => {
    setShowUserMenu(!showUserMenu);
  };

  const getRoleDisplayName = (roles: string[]) => {
    if (roles.includes('ADMIN')) return 'Beheerder';
    if (roles.includes('ACCOUNTANT')) return 'Boekhouder';
    if (roles.includes('OWNER')) return 'Verhuurder';
    if (roles.includes('RENTER')) return 'Huurder';
    return 'Gebruiker';
  };

  const getRoleBadgeColor = (roles: string[]) => {
    if (roles.includes('ADMIN')) return 'bg-red-500';
    if (roles.includes('ACCOUNTANT')) return 'bg-purple-500';
    if (roles.includes('OWNER')) return 'bg-blue-500';
    if (roles.includes('RENTER')) return 'bg-green-500';
    return 'bg-gray-500';
  };

  const getNavItems = () => {
    const baseItems = [
      { href: "/", label: "Home", show: true },
    ];

    if (user) {
      // Dashboard link for logged-in users
      baseItems.push({ href: "/dashboard", label: "Dashboard", show: true });
      
      // Role-specific navigation
      if (canManageCars()) {
        baseItems.push({ href: "/Cars", label: "Auto's", show: true });
      }
      
      baseItems.push({ href: "/Rentals", label: "Verhuur", show: true });
      
      if (isRenter() || isAdmin()) {
        baseItems.push({ href: "/Rents", label: "Huren", show: true });
      }
      
      if (canViewBookkeeping()) {
        baseItems.push({ href: "/dashboard/bookkeeping", label: "Boekhouding", show: true });
      }
      
      if (canManageUsers()) {
        baseItems.push({ href: "/dashboard/users", label: "Gebruikers", show: true });
      }
    } else {
      // Public navigation for non-logged-in users
      baseItems.push(
        { href: "/Cars", label: "Auto's", show: true },
        { href: "/Login", label: "Login", show: true },
        { href: "/Register", label: "Registreren", show: true }
      );
    }

    return baseItems.filter(item => item.show);
  };

  const getDashboardLink = () => {
    if (!user) return "/dashboard";
    
    if (isAdmin()) return "/dashboard/admin";
    if (isAccountant()) return "/dashboard/accountant";
    if (isOwner()) return "/dashboard/owner";
    if (isRenter()) return "/dashboard/renter";
    return "/dashboard";
  };

  return (
    <header className="bg-blue-600 text-white p-4 flex items-center justify-between shadow-lg">
      {/* App title */}
      <Link href="/" className="text-2xl font-bold hover:text-yellow-300 transition-colors">
        Car4Rent
      </Link>

      {/* Navigation */}
      <nav className="flex gap-6 text-lg items-center">
        {getNavItems().map((item) => (
          <Link 
            key={item.href}
            href={item.href} 
            className={`hover:text-yellow-300 transition-colors ${
              router.pathname === item.href ? 'text-yellow-300 font-semibold' : ''
            }`}
          >
            {item.label}
          </Link>
        ))}
      </nav>

      {/* Right side - User info and notifications */}
      <div className="flex items-center gap-4">
        {/* User section */}
        {user ? (
          <div className="relative">
            <button
              onClick={toggleUserMenu}
              className="flex items-center gap-2 hover:text-yellow-300 bg-blue-700 px-3 py-2 rounded-md transition-colors"
            >
              <User size={20} />
              <div className="flex flex-col items-start">
                <span className="font-medium text-sm">{user.username}</span>
                <span className="text-xs opacity-75">{getRoleDisplayName(user.roles)}</span>
              </div>
              {/* Role indicator badge */}
              <div className={`w-2 h-2 rounded-full ${getRoleBadgeColor(user.roles)}`}></div>
            </button>
            
            {/* User dropdown menu */}
            {showUserMenu && (
              <div className="absolute right-0 mt-2 w-56 bg-white text-black rounded-lg shadow-lg z-50 border">
                <div className="p-4 border-b border-gray-200">
                  <div className="font-medium text-gray-900">{user.username}</div>
                  <div className="text-sm text-gray-500">{user.email}</div>
                  <div className="flex items-center gap-2 mt-2">
                    <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium text-white ${getRoleBadgeColor(user.roles)}`}>
                      {getRoleDisplayName(user.roles)}
                    </span>
                  </div>
                </div>
                
                <div className="py-2">
                  <Link 
                    href={getDashboardLink()}
                    className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                    onClick={() => setShowUserMenu(false)}
                  >
                    <BarChart3 size={16} />
                    Dashboard
                  </Link>
                  
                  <Link 
                    href="/profile" 
                    className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                    onClick={() => setShowUserMenu(false)}
                  >
                    <User size={16} />
                    Profiel
                  </Link>
                  
                  <Link 
                    href="/settings" 
                    className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                    onClick={() => setShowUserMenu(false)}
                  >
                    <Settings size={16} />
                    Instellingen
                  </Link>
                  
                  <div className="border-t border-gray-200 my-2"></div>
                  
                  <button
                    onClick={handleLogout}
                    className="w-full text-left flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
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
          <Link 
            href="/Login" 
            className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 px-4 py-2 rounded-md transition-colors"
          >
            <User size={20} />
            <span>Inloggen</span>
          </Link>
        )}

        {/* Notification icon - only show for logged-in users */}
        {user && (
          <div className="relative">
            <button
              className="hover:text-yellow-300 p-2 rounded-md transition-colors"
              aria-label="Notifications"
            >
              <Bell size={24} />
            </button>
            {/* Notification count badge */}
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full px-2 py-1 min-w-[1.25rem] h-5 flex items-center justify-center">
              3
            </span>
          </div>
        )}
      </div>

      {/* Click outside to close menu */}
      {showUserMenu && (
        <div 
          className="fixed inset-0 z-40" 
          onClick={() => setShowUserMenu(false)}
        ></div>
      )}
    </header>
  );
};

export default Header;