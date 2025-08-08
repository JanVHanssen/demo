package be.ucll.se.demo.repository;

import be.ucll.se.demo.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByCarId(Long carId);

    List<Rental> findByOwnerEmail(String ownerEmail);

    List<Rental> findByStartDate(LocalDate startDate);

    List<Rental> findByPickupPoint_City(String city);
}
