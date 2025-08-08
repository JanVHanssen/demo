
package be.ucll.se.demo.service;

import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RentService {

    private final RentRepository rentRepository;
    private final CarRepository carRepository;

    public RentService(RentRepository rentRepository, CarRepository carRepository) {
        this.rentRepository = rentRepository;
        this.carRepository = carRepository;
    }

    public List<Rent> getAllRents() {
        return rentRepository.findAll();
    }

    public Optional<Rent> getRentById(Long id) {
        return rentRepository.findById(id);
    }

    public Rent addRent(Rent rent) {
        // Check of auto bestaat
        Optional<Car> car = carRepository.findById(rent.getCar().getId());
        if (car.isEmpty()) {
            throw new IllegalArgumentException("Car with ID " + rent.getCar().getId() + " does not exist.");
        }

        // Validatie: startDate < endDate
        if (rent.getStartDate().isAfter(rent.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        return rentRepository.save(rent);
    }

    public void deleteRent(Long id) {
        rentRepository.deleteById(id);
    }

    // Basis methodes op basis van standaard repository methodes
    public List<Rent> getRentsByCar(Car car) {
        return rentRepository.findByCar(car);
    }

    // ✅ FIXED: Gebruik nieuwe method name
    public List<Rent> getRentsByRenterEmail(String email) {
        return rentRepository.findByRenterEmail(email);
    }

    public List<Rent> getRentsByNationalRegisterId(String id) {
        return rentRepository.findByRenterInfoNationalRegisterId(id);
    }

    public List<Rent> getActiveOrUpcomingRentsForCar(Car car) {
        return rentRepository.findByCarAndEndDateGreaterThanEqual(car, LocalDate.now());
    }
}