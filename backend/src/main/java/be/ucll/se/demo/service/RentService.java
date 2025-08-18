package be.ucll.se.demo.service;

import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.repository.CarRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RentService {

    @Autowired
    private NotificationService notificationService;

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

        // Save rent first
        Rent savedRent = rentRepository.save(rent);

        // Send notifications
        notificationService.notifyOwnerOfNewBooking(savedRent);
        notificationService.notifyRenterOfConfirmation(savedRent);

        return savedRent;
    }

    public void deleteRent(Long rentId) {
        Optional<Rent> rentOpt = rentRepository.findById(rentId);
        if (rentOpt.isPresent()) {
            Rent rent = rentOpt.get();

            // Verstuur annulering notificaties
            notificationService.notifyBookingCancellation(rent);

            rentRepository.deleteById(rentId);
        } else {
            throw new IllegalArgumentException("Rent with ID " + rentId + " does not exist.");
        }
    }

    // Basis methodes op basis van standaard repository methodes
    public List<Rent> getRentsByCar(Car car) {
        return rentRepository.findByCar(car);
    }

    public List<Rent> getRentsByRenterEmail(String email) {
        return rentRepository.findByRenterEmail(email);
    }

    public List<Rent> getRentsByNationalRegisterId(String id) {
        return rentRepository.findByRenterInfoNationalRegisterId(id);
    }

    public List<Rent> getActiveOrUpcomingRentsForCar(Car car) {
        return rentRepository.findByCarAndEndDateGreaterThanEqual(car, LocalDate.now());
    }

    // Nieuwe methodes voor scheduler/reminder functionaliteit
    public List<Rent> getRentsByStartDate(LocalDate startDate) {
        return rentRepository.findByStartDate(startDate);
    }

    public List<Rent> getRentsByEndDate(LocalDate endDate) {
        return rentRepository.findByEndDate(endDate);
    }

    // Helper methode voor beschikbaarheidscheck
    public boolean isCarAvailableForPeriod(Long carId, LocalDate startDate, LocalDate endDate) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            return false;
        }

        Car car = carOpt.get();
        List<Rent> conflictingRents = rentRepository.findByCarAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                car, endDate, startDate);

        return conflictingRents.isEmpty();
    }
}