package be.ucll.se.demo.dataJpaTest;

import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.RenterInfo;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.repository.RentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RentRepositoryTest {

    @Autowired
    private RentRepository rentRepository;

    @Autowired
    private CarRepository carRepository;

    private Car car;
    private Rent rent;

    @BeforeEach
    void setUp() {
        // Maak en bewaar een auto
        car = new Car();
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setLicensePlate("ABC-123");
        carRepository.save(car);

        // Maak renterInfo
        RenterInfo renterInfo = new RenterInfo();
        renterInfo.setPhoneNumber("0123456789");
        renterInfo.setNationalRegisterId("99.99.99-999.99");
        renterInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        renterInfo.setDrivingLicenseNumber("1234567890");

        // Maak rent
        rent = new Rent(
                car,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "owner@example.com",
                "renter@example.com",
                renterInfo);
        rentRepository.save(rent);
    }

    @Test
    void testFindByCar() {
        List<Rent> rents = rentRepository.findByCar(car);
        assertThat(rents).hasSize(1);
        assertThat(rents.get(0).getCar().getLicensePlate()).isEqualTo("ABC-123");
    }

    @Test
    void testFindByRenterEmail() {
        List<Rent> rents = rentRepository.findByRenterEmail("renter@example.com");
        assertThat(rents).hasSize(1);
        assertThat(rents.get(0).getRenterEmail()).isEqualTo("renter@example.com");
    }

    @Test
    void testFindByRenterInfoNationalRegisterId() {
        List<Rent> rents = rentRepository.findByRenterInfoNationalRegisterId("99.99.99-999.99");
        assertThat(rents).hasSize(1);
        assertThat(rents.get(0).getRenterInfo().getNationalRegisterId()).isEqualTo("99.99.99-999.99");
    }

    @Test
    void testFindByCarAndEndDateGreaterThanEqual() {
        List<Rent> rents = rentRepository.findByCarAndEndDateGreaterThanEqual(car, LocalDate.now());
        assertThat(rents).hasSize(1);
    }

    @Test
    void testFindByCarAndStartDateLessThanEqualAndEndDateGreaterThanEqual() {
        List<Rent> rents = rentRepository.findByCarAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                car,
                LocalDate.now().plusDays(3),
                LocalDate.now());
        assertThat(rents).hasSize(1);
    }
}
