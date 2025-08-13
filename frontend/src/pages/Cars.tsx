import React, { useEffect, useState } from "react";
import { GetStaticProps } from 'next';
import { useTranslation } from 'next-i18next';
import { serverSideTranslations } from 'next-i18next/serverSideTranslations';
import { fetchUserCars, deleteCar } from "../services/CarService";
import { validateToken, isAuthenticated } from "../services/AuthService";
import Link from "next/link";
import { useRouter } from "next/router";
import { Car } from "../Types"; 

export default function CarsPage() {
  const { t } = useTranslation('common');
  const [cars, setCars] = useState<Car[]>([]);
  const [error, setError] = useState("");
  const [isAuthenticatedState, setIsAuthenticatedState] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const checkAuthStatus = async () => {
    if (!isAuthenticated()) {
      setIsAuthenticatedState(false);
      setIsLoading(false);
      router.push("/login");
      return;
    }

    try {
      const isValid = await validateToken();
      if (isValid) {
        setIsAuthenticatedState(true);
      } else {
        setIsAuthenticatedState(false);
        router.push("/login");
      }
    } catch (error) {
      console.error('Token validation error:', error);
      setIsAuthenticatedState(false);
      router.push("/login");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const loadCars = async () => {
    try {
      const data = await fetchUserCars(); 
      setCars(data);
    } catch (err: any) {
      setError(err.message || t('cars.loadError'));
      if (err.message.includes("ingelogd") || err.message.includes("token")) {
        router.push("/login");
      }
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm(t('cars.deleteConfirm'))) {
      return;
    }

    try {
      await deleteCar(id);
      setCars((prev) => prev.filter((car) => car.id !== id));
    } catch (err) {
      alert(t('cars.deleteFailed'));
    }
  };

  useEffect(() => {
    if (isAuthenticatedState) {
      loadCars();
    }
  }, [isAuthenticatedState]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div>{t('common.loading')}</div>
      </div>
    );
  }

  if (!isAuthenticatedState) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <main className="flex-grow p-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">{t('cars.title')}</h1>
          <Link href="/AddCar" passHref>
            <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
              {t('cars.addCar')}
            </button>
          </Link>
        </div>

        {error && <p className="text-red-500 mb-4">{error}</p>}

        {cars.length === 0 ? (
          <div className="flex justify-center items-center mt-20">
            <p className="text-gray-600 text-lg">{t('cars.noCars')}</p>
          </div>
        ) : (
          <ul className="space-y-6">
            {cars.map((car) => (
              <li
                key={car.id}
                className="bg-white p-6 rounded shadow flex flex-col sm:flex-row sm:justify-between sm:items-center"
              >
                <div>
                  <p className="text-xl font-semibold">
                    {car.brand} {car.model}
                  </p>
                  <p>{t('cars.licensePlate')}: {car.licensePlate}</p>
                  <p>{t('cars.type')}: {car.type}</p>
                  <p>{t('cars.numberOfSeats')}: {car.numberOfSeats}</p>
                  <p>{t('cars.numberOfChildSeats')}: {car.numberOfChildSeats}</p>
                  <p>
                    {t('cars.foldingRearSeat')}:{" "}
                    {car.foldingRearSeat ? t('common.yes') : t('common.no')}
                  </p>
                  <p>{t('cars.towbar')}: {car.towbar ? t('common.yes') : t('common.no')}</p>
                  <p>
                    {t('cars.availableForRent')}: {car.available ? t('cars.available') : t('cars.unavailable')}
                  </p>
                  <p>{t('cars.pricePerDay')}: €{car.pricePerDay}</p>
                  <p className="text-sm text-gray-500 mt-1">
                    {t('cars.owner')}: {car.ownerEmail}
                  </p>
                </div>

                <div className="flex flex-col sm:items-end gap-2 mt-4 sm:mt-0">
                  <Link href={`/cars/${car.id}/EditCar`} passHref>
                    <button className="bg-yellow-500 text-white px-3 py-1 rounded hover:bg-yellow-600">
                      {t('common.edit')}
                    </button>
                  </Link>

                  <button
                    onClick={() => handleDelete(car.id)}
                    className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
                  >
                    {t('common.delete')}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}

export const getStaticProps: GetStaticProps = async ({ locale }) => {
  return {
    props: {
      ...(await serverSideTranslations(locale || 'nl', ['common'])),
    },
  };
};