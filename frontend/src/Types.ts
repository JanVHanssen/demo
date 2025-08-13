// Types.ts - Updated to match database schema

export interface Rent {
  id?: number;                     // Optional voor nieuwe rents
  carId: number;
  startDate: string;           
  endDate: string;                 
  ownerEmail: string;
  renterEmail: string;             
  phoneNumber: string;              
  nationalRegisterId: string;
  birthDate: string;                
  drivingLicenseNumber: string;
  createdAt?: string;              // Database timestamp
  car?: Car;                       // Voor populated data
}

export interface RenterInfo {
  email: string;
  phoneNumber: string;
  nationalRegisterId: string;
  birthDate: string; // 'YYYY-MM-DD'
  drivingLicenseNumber: string;
}

export interface Car {
  id: number;
  brand: string;
  model: string;
  licensePlate: string;            // DB: license_plate
  type: string;
  numberOfSeats: number;           // DB: number_of_seats
  numberOfChildSeats: number;      // DB: number_of_child_seats
  foldingRearSeat: boolean;        // DB: folding_rear_seat
  towbar: boolean;
  pricePerDay: number;             // DB: price_per_day
  available: boolean;
  ownerEmail: string;              // DB: owner_email
  createdAt?: string;              // DB: created_at
  updatedAt?: string;              // DB: updated_at
}

export interface Rental {
  id?: number;                     // Optional voor nieuwe rentals
  car: Car;
  carId?: number;                  // Voor POST requests
  startDate: string;               // DB: start_date
  startTime: string;               // DB: start_time
  endDate: string;                 // DB: end_date
  endTime: string;                 // DB: end_time
  pickupPoint?: PickupPoint;       // Derived van pickup fields
  pickupStreet?: string;           // DB: pickup_street
  pickupNumber?: string;           // DB: pickup_number
  pickupPostal?: string;           // DB: pickup_postal
  pickupCity?: string;             // DB: pickup_city
  contact?: Contact;               // Derived van contact fields
  contactName?: string;            // DB: contact_name
  contactPhoneNumber?: string;     // DB: contact_phone_number
  contactEmail?: string;           // DB: contact_email
  ownerEmail: string;              // DB: owner_email
  createdAt?: string;              // DB: created_at
}

export interface PickupPoint {
  street: string;
  number: string;
  postal: string;
  city: string;
}

export interface Contact {
  name?: string;                   // Toegevoegd voor contactName
  phoneNumber: string;
  email: string;
}

// User interfaces voor authentication
export interface AppUser {
  id?: number;
  username: string;
  email: string;
  password?: string;               // Optional - niet altijd meesturen
  roles?: UserRole[];              // User roles
  createdAt?: string;
  updatedAt?: string;
}

export interface UserRole {
  id: number;
  name: 'ADMIN' | 'OWNER' | 'RENTER' | 'ACCOUNTANT';
  createdAt?: string;
}

// Auth related types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: 'OWNER' | 'RENTER';        // Only these roles can register
}

export interface AuthResponse {
  token: string;
  user: AppUser;
  expiresIn: number;
}

// Enum voor car types
export enum CarType {
  SEDAN = 'SEDAN',
  SUV = 'SUV',
  HATCHBACK = 'HATCHBACK',
  COUPE = 'COUPE',
  CONVERTIBLE = 'CONVERTIBLE',
  PICKUP_TRUCK = 'PICKUP_TRUCK'
}

// API Response types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

// Form types voor frontend
export interface CarFormData {
  brand: string;
  model: string;
  licensePlate: string;
  type: CarType;
  numberOfSeats: number;
  numberOfChildSeats: number;
  foldingRearSeat: boolean;
  towbar: boolean;
  pricePerDay: number;
  available: boolean;
}

export interface RentalFormData {
  carId: string;                   // String omdat het van een form komt
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  pickupStreet: string;
  pickupNumber: string;
  pickupPostal: string;
  pickupCity: string;
  contactName: string;
  contactPhoneNumber: string;
  contactEmail: string;
}

export interface RentFormData {
  rentalId: string;               // String omdat het van een form komt
  renterEmail: string;
  phoneNumber: string;
  nationalRegisterId: string;
  birthDate: string;
  drivingLicenseNumber: string;
}