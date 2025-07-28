

export interface AppUser {
  id: number;
  username: string;
  email: string;
  password: string;
}

export interface Car {
  id: number;
  brand: string;
  model: string;
  licensePlate: string;
  ownerEmail: string;
  rentals?: Rental[];
  rents?: Rent[];
}

export interface Rent {
  id: number;
  car: Car;
  startDate: string;  
  endDate: string;
  ownerEmail: string;
  renterEmail: string;
}

export interface Rental {
  id: number;
  car: Car;
  startDate: string;
  endDate: string;
  city: string;
  ownerEmail: string;
}
