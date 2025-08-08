
package be.ucll.se.demo.repository;

import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentRepository extends JpaRepository<Rent, Long> {

        // 1. Vind alle rents voor een bepaalde auto (unchanged)
        List<Rent> findByCar(Car car);

        // 2. ✅ FIXED: Gebruik directe renterEmail veld i.p.v. renterInfo.email
        List<Rent> findByRenterEmail(String email);

        // 3. Vind alle rents op basis van het rijksregisternummer (embedded field -
        // unchanged)
        List<Rent> findByRenterInfoNationalRegisterId(String nationalRegisterId);

        // 4. Vind alle toekomstige of lopende rents voor een bepaalde auto (unchanged)
        List<Rent> findByCarAndEndDateGreaterThanEqual(Car car, LocalDate date);

        // 5. Voor beschikbaarheidscontrole (unchanged)
        List<Rent> findByCarAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        Car car, LocalDate endDate, LocalDate startDate);
}