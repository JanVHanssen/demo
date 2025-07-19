package be.ucll.se.demo.repository;

import be.ucll.se.demo.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
