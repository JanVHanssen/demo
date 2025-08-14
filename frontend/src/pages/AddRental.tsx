import { useState, useEffect, ChangeEvent, FormEvent } from "react";
import { useRouter } from "next/router";
import { useTranslation } from "next-i18next";
import { addRental } from "@/services/RentalService";
import { fetchCarsByOwner } from "@/services/CarService";
import { Car } from "../Types";
import { GetStaticProps } from "next";
import { serverSideTranslations } from "next-i18next/serverSideTranslations";

const AddRental = () => {
  const router = useRouter();
  const { t } = useTranslation('common');
  const [cars, setCars] = useState<Car[]>([]);
  
  const [form, setForm] = useState({
    carId: "",
    startDate: "",
    startTime: "",
    endDate: "",
    endTime: "",
    pickupStreet: "",
    pickupNumber: "",
    pickupPostal: "",
    pickupCity: "",
    contactName: "",
    contactPhoneNumber: "",
    contactEmail: "",
    ownerEmail: "",
  });
  
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const checkAuthStatus = async () => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      setIsAuthenticated(false);
      setIsLoading(false);
      router.push("/Login");
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

      if (response.ok && data.valid) {
        setIsAuthenticated(true);
      } else {
        localStorage.removeItem('token');
        setIsAuthenticated(false);
        router.push("/Login");
      }
    } catch (error) {
      console.error('Fout bij valideren token:', error);
      setIsAuthenticated(false);
      router.push("/Login");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const getEmailFromToken = (token: string): string | null => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.email || payload.sub || null;
    } catch (error) {
      return null;
    }
  };

  useEffect(() => {
    if (!isAuthenticated) return;

    const fetchCars = async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) throw new Error(t('rentals.noTokenFound'));

        const email = getEmailFromToken(token);
        if (!email) throw new Error(t('rentals.noEmailFromToken'));

        const cars = await fetchCarsByOwner(email);
        setCars(cars);
      } catch (error) {
        console.error("Fout bij ophalen van auto's:", error);
      }
    };

    fetchCars();
  }, [isAuthenticated, t]);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!form.carId) {
      alert(t('rentals.selectCar'));
      return;
    }

    const carId = parseInt(form.carId);
    if (isNaN(carId) || carId <= 0) {
      alert(t('rentals.invalidCar'));
      return;
    }

    const requiredFields = [
      'startDate', 'startTime', 'endDate', 'endTime',
      'pickupStreet', 'pickupNumber', 'pickupPostal', 'pickupCity',
      'contactName', 'contactPhoneNumber', 'contactEmail'
    ];

    for (const field of requiredFields) {
      if (!form[field as keyof typeof form]) {
        alert(t('rentals.fieldRequired', { field }));
        return;
      }
    }

    const formData = {
      carId: form.carId,
      startDate: form.startDate,
      startTime: form.startTime,
      endDate: form.endDate,
      endTime: form.endTime,
      pickupStreet: form.pickupStreet,
      pickupNumber: form.pickupNumber,
      pickupPostal: form.pickupPostal,
      pickupCity: form.pickupCity,
      contactName: form.contactName,
      contactPhoneNumber: form.contactPhoneNumber,
      contactEmail: form.contactEmail,
      ownerEmail: form.ownerEmail,
    };

    console.log("Form data to submit:", formData);
    console.log("Selected car:", cars.find(car => car.id === carId));

    try {
      await addRental(formData);
      alert(t('rentals.addSuccess'));
      router.push("/Rentals");
    } catch (err: any) {
      console.error("Fout bij toevoegen rental:", err);
      if (err.message && (err.message.includes("ingelogd") || err.message.includes("token"))) {
        router.push("/Login");
        return;
      }
      alert(t('rentals.addError', { error: err.message }));
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div>{t('common.loading')}</div>
      </div>
    );
  }

  if (!isAuthenticated) return null;
  
  if (cars.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>{t('rentals.noCarsAdded')}</p>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-lg mx-auto">
      <h1 className="text-2xl font-bold mb-4">{t('rentals.addRental')}</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <select
          name="carId"
          value={form.carId}
          onChange={handleChange}
          required
          className="border p-2 w-full"
        >
          <option value="">{t('rentals.selectCar')}</option>
          {cars.map((car) => (
            <option key={car.id} value={car.id}>
              {car.brand} {car.model} ({car.licensePlate})
            </option>
          ))}
        </select>

        <input 
          type="date" 
          name="startDate" 
          value={form.startDate} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="time" 
          name="startTime" 
          value={form.startTime} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="date" 
          name="endDate" 
          value={form.endDate} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="time" 
          name="endTime" 
          value={form.endTime} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />

        <h2 className="font-semibold">{t('rentals.pickupPoint')}</h2>
        <input 
          type="text" 
          name="pickupStreet" 
          placeholder={t('rentals.street')} 
          value={form.pickupStreet} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="text" 
          name="pickupNumber" 
          placeholder={t('rentals.number')} 
          value={form.pickupNumber} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="text" 
          name="pickupPostal" 
          placeholder={t('rentals.postal')} 
          value={form.pickupPostal} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="text" 
          name="pickupCity" 
          placeholder={t('rentals.city')} 
          value={form.pickupCity} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />

        <h2 className="font-semibold">{t('rentals.contactInfo')}</h2>
        <input 
          type="text" 
          name="contactName" 
          placeholder={t('rentals.name')} 
          value={form.contactName} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="tel" 
          name="contactPhoneNumber" 
          placeholder={t('rentals.phoneNumber')} 
          value={form.contactPhoneNumber} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />
        <input 
          type="email" 
          name="contactEmail" 
          placeholder={t('auth.email')} 
          value={form.contactEmail} 
          onChange={handleChange} 
          required 
          className="border p-2 w-full" 
        />

        <button 
          type="submit" 
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          {t('rentals.add')}
        </button>
      </form>
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

export default AddRental;