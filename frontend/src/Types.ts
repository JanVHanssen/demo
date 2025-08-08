

export interface Rent {
  id: number;
  carId: number;
  startDate: string;           
  endDate: string;                 
  ownerEmail: string;
  renterEmail: string;             
  phoneNumber: string;              
  nationalRegisterId: string;
  birthDate: string;                
  drivingLicenseNumber: string;    
  car?: Car;                       
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
  licensePlate: string;
  type: string;
  numberOfSeats: number;
  numberOfChildSeats: number;
  foldingRearSeat: boolean;
  towbar: boolean;
  pricePerDay: number;
  available: boolean;
  ownerEmail: string;
}

export interface Rental {
  id: number;
  car: Car;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  pickupPoint: PickupPoint;
  contact: Contact;
  ownerEmail: string;
}

export interface PickupPoint {
  street: string;
  number: string;
  postal: string;
  city: string;
}

export interface Contact {
  phoneNumber: string;
  email: string;
}

export interface AppUser {
  id: number;
  username: string;
  email: string;
  password: string;
}

export enum CarType {
  SEDAN = 'SEDAN',
  SUV = 'SUV',
  HATCHBACK = 'HATCHBACK',
  COUPE = 'COUPE',
  CONVERTIBLE = 'CONVERTIBLE',
  PICKUP_TRUCK = 'PICKUP_TRUCK'
}


