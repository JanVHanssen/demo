package be.ucll.se.demo.dataJpaTest;

import be.ucll.se.demo.model.*;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.repository.RentalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Opslaan en terugvinden van een Rental")
    void saveAndFindRental() {
        // Arrange: maak een Car
        Car car = new Car();
        car.setBrand("Tesla");
        car.setModel("Model 3");
        car.setLicensePlate("1-ABC-123");
        car = carRepository.save(car);

        // Arrange: maak PickupPoint en Contact
        PickupPoint pickupPoint = new PickupPoint("Main Street", "10", "3000", "Leuven");
        Contact contact = new Contact("Jan Jansen", "012345678", "jan@example.com");

        // Arrange: maak Rental
        Rental rental = new Rental(
                car,
                LocalDate.of(2025, 1, 1),
                LocalTime.of(10, 0),
                LocalDate.of(2025, 1, 5),
                LocalTime.of(18, 0),
                pickupPoint,
                contact,
                "owner@example.com");

        // Act: opslaan
        Rental saved = rentalRepository.save(rental);

        // Assert: ophalen via id
        Rental found = rentalRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCar().getBrand()).isEqualTo("Tesla");
        assertThat(found.getPickupPoint().getCity()).isEqualTo("Leuven");
        assertThat(found.getOwnerEmail()).isEqualTo("owner@example.com");
    }

    @Test
    @DisplayName("FindByOwnerEmail werkt correct")
    void findByOwnerEmail() {
        Car car = new Car();
        car.setBrand("BMW");
        car.setModel("i3");
        car.setLicensePlate("2-DEF-456");
        car = carRepository.save(car);

        PickupPoint pickupPoint = new PickupPoint("Stationstraat", "5", "2000", "Antwerpen");
        Contact contact = new Contact("Piet Peters", "0987654321", "piet@example.com");

        Rental rental = new Rental(
                car,
                LocalDate.of(2025, 2, 1),
                LocalTime.of(9, 0),
                LocalDate.of(2025, 2, 3),
                LocalTime.of(12, 0),
                pickupPoint,
                contact,
                "owner@example.com");
        rentalRepository.save(rental);

        List<Rental> rentals = rentalRepository.findByOwnerEmail("owner@example.com");
        assertThat(rentals).hasSize(1);
        assertThat(rentals.get(0).getCar().getModel()).isEqualTo("i3");
    }
}
