package be.ucll.se.demo.repository;

import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.CarType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByLicensePlate(String licensePlate);

    List<Car> findByOwnerEmail(String ownerEmail);

    List<Car> findByAvailableForRentTrue();

    List<Car> findByType(CarType type);
}
