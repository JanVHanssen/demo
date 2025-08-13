import React, { useEffect, useState } from "react";
import { GetStaticProps } from 'next';
import { useTranslation } from 'next-i18next';
import { serverSideTranslations } from 'next-i18next/serverSideTranslations';
import { useRouter } from "next/router";
import { 
  Users, 
  Search, 
  Filter, 
  Edit3, 
  Trash2, 
  UserPlus, 
  Shield, 
  ShieldCheck,
  Crown,
  Calculator,
  Car,
  Key
} from "lucide-react";
import { isAdmin, isAuthenticated as checkIsAuthenticated, validateToken } from "../services/AuthService";

interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
  lastLogin: string | null;
  isActive: boolean;
  totalCars?: number;
  totalRentals?: number;
}

export default function AdminUsersPage() {
  const { t } = useTranslation('common');
  const router = useRouter();
  const [users, setUsers] = useState<User[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticatedState, setIsAuthenticatedState] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [error, setError] = useState("");

  // Check authentication and admin status
  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    if (!checkIsAuthenticated()) {
      router.push("/login");
      return;
    }

    try {
      const isValid = await validateToken();
      if (isValid && isAdmin()) {
        setIsAuthenticatedState(true);
        loadUsers();
      } else {
        router.push("/dashboard");
      }
    } catch (error) {
      console.error('Auth check failed:', error);
      router.push("/login");
    } finally {
      setIsLoading(false);
    }
  };

  // Mock data - replace with actual API call
  const loadUsers = async () => {
    try {
      // Replace this with actual API call
      const mockUsers: User[] = [
        {
          id: "1",
          username: "admin",
          email: "admin@car4rent.com",
          roles: ["ADMIN"],
          createdAt: "2024-01-15",
          lastLogin: "2024-03-15",
          isActive: true,
          totalCars: 0,
          totalRentals: 0
        },
        {
          id: "2",
          username: "john_owner",
          email: "john@example.com",
          roles: ["OWNER"],
          createdAt: "2024-02-10",
          lastLogin: "2024-03-14",
          isActive: true,
          totalCars: 3,
          totalRentals: 12
        },
        {
          id: "3",
          username: "sarah_renter",
          email: "sarah@example.com",
          roles: ["RENTER"],
          createdAt: "2024-02-20",
          lastLogin: "2024-03-13",
          isActive: true,
          totalCars: 0,
          totalRentals: 5
        },
        {
          id: "4",
          username: "mike_accountant",
          email: "mike@car4rent.com",
          roles: ["ACCOUNTANT"],
          createdAt: "2024-01-20",
          lastLogin: "2024-03-12",
          isActive: true,
          totalCars: 0,
          totalRentals: 0
        },
        {
          id: "5",
          username: "lisa_multi",
          email: "lisa@example.com",
          roles: ["OWNER", "RENTER"],
          createdAt: "2024-03-01",
          lastLogin: "2024-03-11",
          isActive: false,
          totalCars: 1,
          totalRentals: 8
        }
      ];
      
      setUsers(mockUsers);
      setFilteredUsers(mockUsers);
    } catch (err: any) {
      setError(t('admin.users.loadError'));
    }
  };

  // Filter users based on search and role filter
  useEffect(() => {
    let filtered = users.filter(user => 
      user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (roleFilter !== "ALL") {
      filtered = filtered.filter(user => user.roles.includes(roleFilter));
    }

    setFilteredUsers(filtered);
  }, [users, searchTerm, roleFilter]);

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'ADMIN': return <Crown size={16} className="text-red-500" />;
      case 'ACCOUNTANT': return <Calculator size={16} className="text-purple-500" />;
      case 'OWNER': return <Car size={16} className="text-blue-500" />;
      case 'RENTER': return <Key size={16} className="text-green-500" />;
      default: return <Shield size={16} className="text-gray-500" />;
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case 'ADMIN': return 'bg-red-100 text-red-800 border-red-200';
      case 'ACCOUNTANT': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'OWNER': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'RENTER': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const handleEditUser = (user: User) => {
    setEditingUser(user);
    setShowEditModal(true);
  };

  const handleDeleteUser = async (userId: string) => {
    if (!window.confirm(t('admin.users.deleteConfirm'))) {
      return;
    }

    try {
      // Replace with actual API call
      setUsers(users.filter(user => user.id !== userId));
      // Show success message
    } catch (err) {
      setError(t('admin.users.deleteError'));
    }
  };

  const handleToggleUserStatus = async (userId: string) => {
    try {
      // Replace with actual API call
      setUsers(users.map(user => 
        user.id === userId 
          ? { ...user, isActive: !user.isActive }
          : user
      ));
    } catch (err) {
      setError(t('admin.users.toggleError'));
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">{t('common.loading')}</div>
      </div>
    );
  }

  if (!isAuthenticatedState) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
              <Users className="w-5 h-5 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900">{t('admin.users.title')}</h1>
          </div>
          <p className="text-gray-600">{t('admin.users.description')}</p>
        </div>

        {/* Controls */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
            <div className="flex flex-col sm:flex-row gap-4 flex-1">
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <input
                  type="text"
                  placeholder={t('admin.users.searchPlaceholder')}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent w-full sm:w-80"
                />
              </div>

              {/* Role Filter */}
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <select
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value)}
                  className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none bg-white"
                >
                  <option value="ALL">{t('admin.users.allRoles')}</option>
                  <option value="ADMIN">{t('roles.admin')}</option>
                  <option value="ACCOUNTANT">{t('roles.accountant')}</option>
                  <option value="OWNER">{t('roles.owner')}</option>
                  <option value="RENTER">{t('roles.renter')}</option>
                </select>
              </div>
            </div>

            {/* Add User Button */}
            <button className="flex items-center space-x-2 bg-gradient-to-r from-blue-600 to-purple-600 text-white px-4 py-2 rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 shadow-md hover:shadow-lg">
              <UserPlus size={18} />
              <span>{t('admin.users.addUser')}</span>
            </button>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-gray-200">
            <div className="text-center">
              <div className="text-2xl font-bold text-gray-900">{users.length}</div>
              <div className="text-sm text-gray-600">{t('admin.users.totalUsers')}</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">{users.filter(u => u.isActive).length}</div>
              <div className="text-sm text-gray-600">{t('admin.users.activeUsers')}</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{users.filter(u => u.roles.includes('OWNER')).length}</div>
              <div className="text-sm text-gray-600">{t('roles.owner')}s</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">{users.filter(u => u.roles.includes('RENTER')).length}</div>
              <div className="text-sm text-gray-600">{t('roles.renter')}s</div>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {/* Users Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.user')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.roles')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.stats')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.lastLogin')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.status')}
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {t('admin.users.actions')}
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50 transition-colors duration-150">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="w-10 h-10 bg-gradient-to-br from-gray-400 to-gray-600 rounded-full flex items-center justify-center text-white font-medium text-sm">
                          {user.username.charAt(0).toUpperCase()}
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{user.username}</div>
                          <div className="text-sm text-gray-500">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex flex-wrap gap-1">
                        {user.roles.map((role) => (
                          <span
                            key={role}
                            className={`inline-flex items-center space-x-1 px-2 py-1 rounded-full text-xs font-medium border ${getRoleBadgeColor(role)}`}
                          >
                            {getRoleIcon(role)}
                            <span>{t(`roles.${role.toLowerCase()}`)}</span>
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <div className="space-y-1">
                        <div>{user.totalCars} {t('admin.users.cars')}</div>
                        <div>{user.totalRentals} {t('admin.users.rentals')}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.lastLogin ? new Date(user.lastLogin).toLocaleDateString() : t('admin.users.neverLoggedIn')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={() => handleToggleUserStatus(user.id)}
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                          user.isActive
                            ? 'bg-green-100 text-green-800 hover:bg-green-200'
                            : 'bg-red-100 text-red-800 hover:bg-red-200'
                        } transition-colors duration-150`}
                      >
                        {user.isActive ? (
                          <>
                            <ShieldCheck size={14} className="mr-1" />
                            {t('admin.users.active')}
                          </>
                        ) : (
                          <>
                            <Shield size={14} className="mr-1" />
                            {t('admin.users.inactive')}
                          </>
                        )}
                      </button>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() => handleEditUser(user)}
                          className="text-blue-600 hover:text-blue-900 p-2 hover:bg-blue-50 rounded-lg transition-colors duration-150"
                          title={t('common.edit')}
                        >
                          <Edit3 size={16} />
                        </button>
                        <button
                          onClick={() => handleDeleteUser(user.id)}
                          className="text-red-600 hover:text-red-900 p-2 hover:bg-red-50 rounded-lg transition-colors duration-150"
                          title={t('common.delete')}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredUsers.length === 0 && (
            <div className="text-center py-12">
              <Users className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">{t('admin.users.noUsers')}</h3>
              <p className="mt-1 text-sm text-gray-500">{t('admin.users.noUsersDescription')}</p>
            </div>
          )}
        </div>

        {/* Edit User Modal */}
        {showEditModal && editingUser && (
          <UserEditModal
            user={editingUser}
            onClose={() => setShowEditModal(false)}
            onSave={(updatedUser) => {
              setUsers(users.map(u => u.id === updatedUser.id ? updatedUser : u));
              setShowEditModal(false);
            }}
          />
        )}
      </div>
    </div>
  );
}

// User Edit Modal Component
interface UserEditModalProps {
  user: User;
  onClose: () => void;
  onSave: (user: User) => void;
}

const UserEditModal: React.FC<UserEditModalProps> = ({ user, onClose, onSave }) => {
  const { t } = useTranslation('common');
  const [formData, setFormData] = useState({
    username: user.username,
    email: user.email,
    roles: user.roles,
    isActive: user.isActive
  });

  const availableRoles = ['ADMIN', 'ACCOUNTANT', 'OWNER', 'RENTER'];

  const handleRoleToggle = (role: string) => {
    if (formData.roles.includes(role)) {
      setFormData({
        ...formData,
        roles: formData.roles.filter(r => r !== role)
      });
    } else {
      setFormData({
        ...formData,
        roles: [...formData.roles, role]
      });
    }
  };

  const handleSave = () => {
    onSave({
      ...user,
      ...formData
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-lg max-w-md w-full mx-4">
        <div className="p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            {t('admin.users.editUser')}
          </h3>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {t('admin.users.username')}
              </label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {t('auth.email')}
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('admin.users.roles')}
              </label>
              <div className="space-y-2">
                {availableRoles.map((role) => (
                  <label key={role} className="flex items-center">
                    <input
                      type="checkbox"
                      checked={formData.roles.includes(role)}
                      onChange={() => handleRoleToggle(role)}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">
                      {t(`roles.${role.toLowerCase()}`)}
                    </span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.isActive}
                  onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                  className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700">
                  {t('admin.users.activeAccount')}
                </span>
              </label>
            </div>
          </div>

          <div className="flex justify-end space-x-3 mt-6">
            <button
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors duration-150"
            >
              {t('common.cancel')}
            </button>
            <button
              onClick={handleSave}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors duration-150"
            >
              {t('common.save')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export const getStaticProps: GetStaticProps = async ({ locale }) => {
  return {
    props: {
      ...(await serverSideTranslations(locale || 'nl', ['common'])),
    },
  };
};