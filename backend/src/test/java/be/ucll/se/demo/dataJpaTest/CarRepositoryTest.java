package be.ucll.se.demo.dataJpaTest;

import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Should save and find car by license plate")
    void testFindByLicensePlate() {
        // arrange
        Car car = new Car("Toyota", "Corolla", "ABC-123", "owner@example.com");
        car.setType(CarType.SEDAN);
        car.setAvailableForRent(true);
        carRepository.save(car);

        // act
        Optional<Car> found = carRepository.findByLicensePlate("ABC-123");

        // assert
        assertThat(found).isPresent();
        assertThat(found.get().getBrand()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("Should find all available cars")
    void testFindAvailableCars() {
        // arrange
        Car availableCar = new Car("Tesla", "Model 3", "TES-001", "elon@example.com");
        availableCar.setType(CarType.SEDAN);
        availableCar.setAvailableForRent(true);

        Car unavailableCar = new Car("BMW", "X5", "BMW-999", "bmw@example.com");
        unavailableCar.setType(CarType.SUV);
        unavailableCar.setAvailableForRent(false);

        carRepository.save(availableCar);
        carRepository.save(unavailableCar);

        // act
        List<Car> availableCars = carRepository.findByAvailableForRentTrue();

        // assert
        assertThat(availableCars)
                .hasSize(1)
                .first()
                .extracting(Car::getLicensePlate)
                .isEqualTo("TES-001");
    }

    @Test
    @DisplayName("Should find cars by owner email")
    void testFindByOwnerEmail() {
        // arrange
        Car car1 = new Car("Ford", "Focus", "FOC-123", "john@example.com");
        car1.setType(CarType.HATCHBACK);
        car1.setAvailableForRent(true);

        Car car2 = new Car("Audi", "A4", "AUD-456", "john@example.com");
        car2.setType(CarType.SEDAN);
        car2.setAvailableForRent(false);

        Car otherOwnerCar = new Car("Opel", "Corsa", "OPE-789", "mary@example.com");
        otherOwnerCar.setType(CarType.HATCHBACK);
        otherOwnerCar.setAvailableForRent(true);

        carRepository.save(car1);
        carRepository.save(car2);
        carRepository.save(otherOwnerCar);

        // act
        List<Car> johnsCars = carRepository.findByOwnerEmail("john@example.com");

        // assert
        assertThat(johnsCars)
                .hasSize(2)
                .extracting(Car::getLicensePlate)
                .containsExactlyInAnyOrder("FOC-123", "AUD-456");
    }
}
