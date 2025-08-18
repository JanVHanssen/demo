package be.ucll.se.demo.repository;

import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentRepository extends JpaRepository<Rent, Long> {

        // 1. Vind alle rents voor een bepaalde auto
        List<Rent> findByCar(Car car);

        List<Rent> findByRenterEmail(String email);

        // 3. Vind alle rents op basis van het rijksregisternummer (embedded field)
        List<Rent> findByRenterInfoNationalRegisterId(String nationalRegisterId);

        // 4. Vind alle toekomstige of lopende rents voor een bepaalde auto
        List<Rent> findByCarAndEndDateGreaterThanEqual(Car car, LocalDate date);

        List<Rent> findByCarAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        Car car, LocalDate endDate, LocalDate startDate);

        List<Rent> findByStartDate(LocalDate startDate);

        List<Rent> findByEndDate(LocalDate endDate);
}