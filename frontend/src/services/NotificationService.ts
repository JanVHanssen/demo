interface Notification {
  id: number;
  type: 'NEW_BOOKING' | 'BOOKING_CONFIRMATION' | 'BOOKING_CANCELLED' | 'RENTAL_REMINDER' | 'RETURN_REMINDER' | 'ACCOUNT_ENABLED' | 'ACCOUNT_DISABLED' | 'SYSTEM_ANNOUNCEMENT';
  status: 'PENDING' | 'SENT' | 'READ' | 'FAILED';
  title: string;
  message: string;
  relatedEntityId?: number;
  relatedEntityType?: string;
  createdAt: string;
  readAt?: string;
}

interface NotificationResponse {
  unreadCount: number;
}

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

class NotificationService {
  private getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  }

  async getUserNotifications(userEmail: string): Promise<Notification[]> {
    try {
      const response = await fetch(`${BASE_URL}/notifications?userEmail=${encodeURIComponent(userEmail)}`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching notifications:', error);
      throw error;
    }
  }

  async getUnreadNotifications(userEmail: string): Promise<Notification[]> {
    try {
      const response = await fetch(`${BASE_URL}/notifications/unread?userEmail=${encodeURIComponent(userEmail)}`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching unread notifications:', error);
      throw error;
    }
  }

  async getUnreadCount(userEmail: string): Promise<number> {
    try {
      const response = await fetch(`${BASE_URL}/notifications/unread/count?userEmail=${encodeURIComponent(userEmail)}`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: NotificationResponse = await response.json();
      return data.unreadCount;
    } catch (error) {
      console.error('Error fetching unread count:', error);
      return 0;
    }
  }

  async markAsRead(notificationId: number): Promise<void> {
    try {
      const response = await fetch(`${BASE_URL}/notifications/${notificationId}/read`, {
        method: 'PUT',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
    } catch (error) {
      console.error('Error marking notification as read:', error);
      throw error;
    }
  }

  async markAllAsRead(userEmail: string): Promise<void> {
    try {
      const response = await fetch(`${BASE_URL}/notifications/read-all?userEmail=${encodeURIComponent(userEmail)}`, {
        method: 'PUT',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      throw error;
    }
  }

  getNotificationIcon(type: Notification['type']): string {
    const icons = {
      'NEW_BOOKING': '🚗',
      'BOOKING_CONFIRMATION': '✅',
      'BOOKING_CANCELLED': '❌',
      'RENTAL_REMINDER': '⏰',
      'RETURN_REMINDER': '🔄',
      'ACCOUNT_ENABLED': '🔓',
      'ACCOUNT_DISABLED': '🔒',
      'SYSTEM_ANNOUNCEMENT': '📢'
    };
    return icons[type] || '📧';
  }

  getNotificationColor(type: Notification['type']): string {
    const colors = {
      'NEW_BOOKING': 'text-blue-600 bg-blue-50',
      'BOOKING_CONFIRMATION': 'text-green-600 bg-green-50',
      'BOOKING_CANCELLED': 'text-red-600 bg-red-50',
      'RENTAL_REMINDER': 'text-orange-600 bg-orange-50',
      'RETURN_REMINDER': 'text-purple-600 bg-purple-50',
      'ACCOUNT_ENABLED': 'text-green-600 bg-green-50',
      'ACCOUNT_DISABLED': 'text-red-600 bg-red-50',
      'SYSTEM_ANNOUNCEMENT': 'text-indigo-600 bg-indigo-50'
    };
    return colors[type] || 'text-gray-600 bg-gray-50';
  }

  formatTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

    if (diffInMinutes < 1) return 'Nu';
    if (diffInMinutes < 60) return `${diffInMinutes}m geleden`;
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours}u geleden`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) return `${diffInDays}d geleden`;
    
    return date.toLocaleDateString('nl-NL');
  }
}

export default new NotificationService();
export type { Notification };