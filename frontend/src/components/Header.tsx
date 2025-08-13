import React, { useState, useEffect } from "react";
import { Bell, User, LogOut, Settings, BarChart3, ChevronDown } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/router";
import { useTranslation } from 'next-i18next';
import LanguageSwitcher from './LanguageSwitcher';
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
  const { t } = useTranslation('common');
  const router = useRouter();

  useEffect(() => {
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
      console.error('Token validation error:', error);
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
    if (roles.includes('ADMIN')) return t('roles.admin');
    if (roles.includes('ACCOUNTANT')) return t('roles.accountant');
    if (roles.includes('OWNER')) return t('roles.owner');
    if (roles.includes('RENTER')) return t('roles.renter');
    return 'User';
  };

  const getRoleBadgeColor = (roles: string[]) => {
    if (roles.includes('ADMIN')) return 'bg-gradient-to-r from-red-500 to-red-600';
    if (roles.includes('ACCOUNTANT')) return 'bg-gradient-to-r from-purple-500 to-purple-600';
    if (roles.includes('OWNER')) return 'bg-gradient-to-r from-blue-500 to-blue-600';
    if (roles.includes('RENTER')) return 'bg-gradient-to-r from-green-500 to-green-600';
    return 'bg-gradient-to-r from-gray-500 to-gray-600';
  };

  const getNavItems = () => {
    const baseItems = [
      { href: "/", label: t('navigation.home'), show: true },
    ];

    if (user) {
      baseItems.push({ href: "/dashboard", label: t('navigation.dashboard'), show: true });
      
      if (canManageCars()) {
        baseItems.push({ href: "/Cars", label: t('navigation.cars'), show: true });
      }
      
      baseItems.push({ href: "/Rentals", label: t('navigation.rentals'), show: true });
      
      if (isRenter() || isAdmin()) {
        baseItems.push({ href: "/Rents", label: t('navigation.rent'), show: true });
      }
      
      if (canViewBookkeeping()) {
        baseItems.push({ href: "/dashboard/bookkeeping", label: t('navigation.bookkeeping'), show: true });
      }
      
      if (canManageUsers()) {
        baseItems.push({ href: "/AdminUsers", label: t('navigation.users'), show: true });
      }
    } else {
      baseItems.push(
        { href: "/Login", label: t('navigation.login'), show: true },
        { href: "/Register", label: t('navigation.register'), show: true }
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
    <header className="bg-white/95 backdrop-blur-md border-b border-gray-200/50 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo */}
          <Link href="/" className="flex items-center space-x-2 group">
            <div className="w-8 h-8 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center group-hover:scale-105 transition-transform duration-200">
              <span className="text-white font-bold text-sm">C4R</span>
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent group-hover:from-purple-600 group-hover:to-blue-600 transition-all duration-300">
              Car4Rent
            </span>
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center space-x-1">
            {getNavItems().map((item) => (
              <Link 
                key={item.href}
                href={item.href} 
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-gray-100 relative group ${
                  router.pathname === item.href 
                    ? 'text-blue-600 bg-blue-50' 
                    : 'text-gray-700 hover:text-gray-900'
                }`}
              >
                {item.label}
                {router.pathname === item.href && (
                  <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-blue-600 rounded-full"></div>
                )}
              </Link>
            ))}
          </nav>

          {/* Right side */}
          <div className="flex items-center space-x-4">
            {/* Language Switcher - More prominent with visible styling */}
            <div className="flex items-center bg-gray-100 hover:bg-gray-200 rounded-lg px-3 py-2 transition-colors duration-200 border border-gray-200">
              <LanguageSwitcher />
            </div>

            {/* User section */}
            {user ? (
              <div className="relative">
                {/* Notifications */}
                <button className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors duration-200 mr-2">
                  <Bell size={20} />
                  <span className="absolute -top-1 -right-1 bg-gradient-to-r from-red-500 to-red-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-medium shadow-lg">
                    3
                  </span>
                </button>

                {/* User Menu Button */}
                <button
                  onClick={toggleUserMenu}
                  className="flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-gray-100 transition-all duration-200 group"
                >
                  <div className="w-8 h-8 bg-gradient-to-br from-gray-400 to-gray-600 rounded-full flex items-center justify-center text-white font-medium text-sm">
                    {user.username.charAt(0).toUpperCase()}
                  </div>
                  <div className="hidden sm:block text-left">
                    <div className="text-sm font-medium text-gray-900">{user.username}</div>
                    <div className="text-xs text-gray-500">{getRoleDisplayName(user.roles)}</div>
                  </div>
                  <ChevronDown 
                    size={16} 
                    className={`text-gray-400 transition-transform duration-200 ${showUserMenu ? 'rotate-180' : ''}`} 
                  />
                </button>
                
                {/* User dropdown menu */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-lg border border-gray-200/50 z-50 overflow-hidden backdrop-blur-md">
                    {/* User info header */}
                    <div className="p-4 bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-200/50">
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 bg-gradient-to-br from-gray-400 to-gray-600 rounded-full flex items-center justify-center text-white font-medium">
                          {user.username.charAt(0).toUpperCase()}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="font-medium text-gray-900 truncate">{user.username}</div>
                          <div className="text-sm text-gray-500 truncate">{user.email}</div>
                        </div>
                      </div>
                      <div className="mt-3">
                        <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium text-white ${getRoleBadgeColor(user.roles)} shadow-sm`}>
                          {getRoleDisplayName(user.roles)}
                        </span>
                      </div>
                    </div>
                    
                    {/* Menu items */}
                    <div className="py-2">
                      <Link 
                        href={getDashboardLink()}
                        className="flex items-center space-x-3 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors duration-200 group"
                        onClick={() => setShowUserMenu(false)}
                      >
                        <BarChart3 size={18} className="text-gray-400 group-hover:text-blue-600" />
                        <span className="group-hover:text-gray-900">{t('navigation.dashboard')}</span>
                      </Link>
                      
                      <Link 
                        href="/profile" 
                        className="flex items-center space-x-3 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors duration-200 group"
                        onClick={() => setShowUserMenu(false)}
                      >
                        <User size={18} className="text-gray-400 group-hover:text-blue-600" />
                        <span className="group-hover:text-gray-900">{t('header.profile')}</span>
                      </Link>
                      
                      <Link 
                        href="/settings" 
                        className="flex items-center space-x-3 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors duration-200 group"
                        onClick={() => setShowUserMenu(false)}
                      >
                        <Settings size={18} className="text-gray-400 group-hover:text-blue-600" />
                        <span className="group-hover:text-gray-900">{t('header.settings')}</span>
                      </Link>
                      
                      <div className="border-t border-gray-200/50 my-2"></div>
                      
                      <button
                        onClick={handleLogout}
                        className="w-full text-left flex items-center space-x-3 px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition-colors duration-200 group"
                      >
                        <LogOut size={18} className="text-red-400 group-hover:text-red-600" />
                        <span className="group-hover:text-red-700">{t('navigation.logout')}</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link 
                  href="/Login" 
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-all duration-200"
                >
                  {t('navigation.login')}
                </Link>
                <Link 
                  href="/Register" 
                  className="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 shadow-md hover:shadow-lg transform hover:scale-105"
                >
                  {t('navigation.register')}
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Navigation (optional) */}
      <div className="md:hidden border-t border-gray-200/50 bg-white/95 backdrop-blur-md">
        <nav className="px-4 py-2 space-y-1">
          {getNavItems().map((item) => (
            <Link 
              key={item.href}
              href={item.href} 
              className={`block px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200 ${
                router.pathname === item.href 
                  ? 'text-blue-600 bg-blue-50' 
                  : 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
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