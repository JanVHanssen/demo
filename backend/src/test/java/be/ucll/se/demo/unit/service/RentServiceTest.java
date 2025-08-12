package be.ucll.se.demo.unit.service;

import be.ucll.se.demo.model.RenterInfo;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.service.RentService;
import be.ucll.se.demo.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentServiceTest {

    @Mock
    private RentRepository rentRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private RentService rentService;

    private Rent testRent;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testCar = createTestCar();
        testRent = createTestRent();
    }

    // ===== GET ALL RENTS TESTS =====
    @Test
    void getAllRents_ShouldReturnListOfRents() {
        // Given
        List<Rent> rents = Arrays.asList(testRent, createTestRent());
        when(rentRepository.findAll()).thenReturn(rents);

        // When
        List<Rent> result = rentService.getAllRents();

        // Then
        assertThat(result).hasSize(2);
        verify(rentRepository).findAll();
    }

    @Test
    void getAllRents_WhenNoRentsExist_ShouldReturnEmptyList() {
        // Given
        when(rentRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Rent> result = rentService.getAllRents();

        // Then
        assertThat(result).isEmpty();
        verify(rentRepository).findAll();
    }

    // ===== GET RENT BY ID TESTS =====
    @Test
    void getRentById_WhenRentExists_ShouldReturnRent() {
        // Given
        Long rentId = 1L;
        when(rentRepository.findById(rentId)).thenReturn(Optional.of(testRent));

        // When
        Optional<Rent> result = rentService.getRentById(rentId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRent);
        verify(rentRepository).findById(rentId);
    }

    @Test
    void getRentById_WhenRentDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long rentId = 999L;
        when(rentRepository.findById(rentId)).thenReturn(Optional.empty());

        // When
        Optional<Rent> result = rentService.getRentById(rentId);

        // Then
        assertThat(result).isEmpty();
        verify(rentRepository).findById(rentId);
    }

    // ===== ADD RENT TESTS =====
    @Test
    void addRent_WhenValidRent_ShouldSaveAndReturnRent() {
        // Given
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(rentRepository.save(testRent)).thenReturn(testRent);

        // When
        Rent result = rentService.addRent(testRent);

        // Then
        assertThat(result).isEqualTo(testRent);
        verify(carRepository).findById(carId);
        verify(rentRepository).save(testRent);
    }

    @Test
    void addRent_WhenCarDoesNotExist_ShouldThrowIllegalArgumentException() {
        // Given
        Long nonExistentCarId = 999L;
        // Use reflection to set car ID since setId doesn't exist
        try {
            java.lang.reflect.Field carIdField = Car.class.getDeclaredField("id");
            carIdField.setAccessible(true);
            carIdField.set(testRent.getCar(), nonExistentCarId);
        } catch (Exception e) {
            // Create new car with different ID for this test
            Car testCarWithDifferentId = createTestCar();
            try {
                java.lang.reflect.Field idField = Car.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(testCarWithDifferentId, nonExistentCarId);
                testRent.setCar(testCarWithDifferentId);
            } catch (Exception ex) {
                // Fallback - just use the existing car
            }
        }
        when(carRepository.findById(nonExistentCarId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rentService.addRent(testRent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car with ID " + nonExistentCarId + " does not exist.");

        verify(carRepository).findById(nonExistentCarId);
        verify(rentRepository, never()).save(any());
    }

    @Test
    void addRent_WhenStartDateAfterEndDate_ShouldThrowIllegalArgumentException() {
        // Given
        Long carId = 1L;
        testRent.setStartDate(LocalDate.now().plusDays(5));
        testRent.setEndDate(LocalDate.now().plusDays(2)); // Earlier than start date
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));

        // When & Then
        assertThatThrownBy(() -> rentService.addRent(testRent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be before end date.");

        verify(carRepository).findById(carId);
        verify(rentRepository, never()).save(any());
    }

    @Test
    void addRent_WhenStartDateEqualsEndDate_ShouldNotThrowException() {
        // Given
        Long carId = 1L;
        LocalDate sameDate = LocalDate.now().plusDays(1);
        testRent.setStartDate(sameDate);
        testRent.setEndDate(sameDate);
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(rentRepository.save(testRent)).thenReturn(testRent);

        // When
        Rent result = rentService.addRent(testRent);

        // Then
        assertThat(result).isEqualTo(testRent);
        verify(carRepository).findById(carId);
        verify(rentRepository).save(testRent);
    }

    @Test
    void addRent_WhenValidDateRange_ShouldSucceed() {
        // Given
        Long carId = 1L;
        testRent.setStartDate(LocalDate.now().plusDays(1));
        testRent.setEndDate(LocalDate.now().plusDays(5));
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(rentRepository.save(testRent)).thenReturn(testRent);

        // When
        Rent result = rentService.addRent(testRent);

        // Then
        assertThat(result).isEqualTo(testRent);
        verify(carRepository).findById(carId);
        verify(rentRepository).save(testRent);
    }

    // ===== DELETE RENT TESTS =====
    @Test
    void deleteRent_ShouldCallRepositoryDeleteById() {
        // Given
        Long rentId = 1L;

        // When
        rentService.deleteRent(rentId);

        // Then
        verify(rentRepository).deleteById(rentId);
    }

    @Test
    void deleteRent_WithNullId_ShouldStillCallRepository() {
        // Given
        Long rentId = null;

        // When
        rentService.deleteRent(rentId);

        // Then
        verify(rentRepository).deleteById(rentId);
    }

    // ===== GET RENTS BY CAR TESTS =====
    @Test
    void getRentsByCar_ShouldReturnRentsForSpecificCar() {
        // Given
        List<Rent> rents = Arrays.asList(testRent, createTestRent());
        when(rentRepository.findByCar(testCar)).thenReturn(rents);

        // When
        List<Rent> result = rentService.getRentsByCar(testCar);

        // Then
        assertThat(result).hasSize(2);
        verify(rentRepository).findByCar(testCar);
    }

    @Test
    void getRentsByCar_WhenNoRentsForCar_ShouldReturnEmptyList() {
        // Given
        when(rentRepository.findByCar(testCar)).thenReturn(Arrays.asList());

        // When
        List<Rent> result = rentService.getRentsByCar(testCar);

        // Then
        assertThat(result).isEmpty();
        verify(rentRepository).findByCar(testCar);
    }

    // ===== GET RENTS BY RENTER EMAIL TESTS =====
    @Test
    void getRentsByRenterEmail_ShouldReturnRentsForEmail() {
        // Given
        String email = "renter@example.com";
        List<Rent> rents = Arrays.asList(testRent);
        when(rentRepository.findByRenterEmail(email)).thenReturn(rents);

        // When
        List<Rent> result = rentService.getRentsByRenterEmail(email);

        // Then
        assertThat(result).hasSize(1);
        verify(rentRepository).findByRenterEmail(email);
    }

    @Test
    void getRentsByRenterEmail_WhenNoRentsForEmail_ShouldReturnEmptyList() {
        // Given
        String email = "nonexistent@example.com";
        when(rentRepository.findByRenterEmail(email)).thenReturn(Arrays.asList());

        // When
        List<Rent> result = rentService.getRentsByRenterEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(rentRepository).findByRenterEmail(email);
    }

    // ===== GET RENTS BY NATIONAL REGISTER ID TESTS =====
    @Test
    void getRentsByNationalRegisterId_ShouldReturnRentsForId() {
        // Given
        String nationalId = "85.01.01-123.45";
        List<Rent> rents = Arrays.asList(testRent);
        when(rentRepository.findByRenterInfoNationalRegisterId(nationalId)).thenReturn(rents);

        // When
        List<Rent> result = rentService.getRentsByNationalRegisterId(nationalId);

        // Then
        assertThat(result).hasSize(1);
        verify(rentRepository).findByRenterInfoNationalRegisterId(nationalId);
    }

    @Test
    void getRentsByNationalRegisterId_WhenNoRentsForId_ShouldReturnEmptyList() {
        // Given
        String nationalId = "00.00.00-000.00";
        when(rentRepository.findByRenterInfoNationalRegisterId(nationalId)).thenReturn(Arrays.asList());

        // When
        List<Rent> result = rentService.getRentsByNationalRegisterId(nationalId);

        // Then
        assertThat(result).isEmpty();
        verify(rentRepository).findByRenterInfoNationalRegisterId(nationalId);
    }

    // ===== GET ACTIVE OR UPCOMING RENTS TESTS =====
    @Test
    void getActiveOrUpcomingRentsForCar_ShouldReturnCurrentAndFutureRents() {
        // Given
        LocalDate today = LocalDate.now();
        List<Rent> activeRents = Arrays.asList(testRent);
        when(rentRepository.findByCarAndEndDateGreaterThanEqual(testCar, today)).thenReturn(activeRents);

        // When
        List<Rent> result = rentService.getActiveOrUpcomingRentsForCar(testCar);

        // Then
        assertThat(result).hasSize(1);
        verify(rentRepository).findByCarAndEndDateGreaterThanEqual(testCar, today);
    }

    @Test
    void getActiveOrUpcomingRentsForCar_WhenNoActiveRents_ShouldReturnEmptyList() {
        // Given
        LocalDate today = LocalDate.now();
        when(rentRepository.findByCarAndEndDateGreaterThanEqual(testCar, today)).thenReturn(Arrays.asList());

        // When
        List<Rent> result = rentService.getActiveOrUpcomingRentsForCar(testCar);

        // Then
        assertThat(result).isEmpty();
        verify(rentRepository).findByCarAndEndDateGreaterThanEqual(testCar, today);
    }

    @Test
    void getActiveOrUpcomingRentsForCar_ShouldUseCurrentDate() {
        // Given
        LocalDate expectedDate = LocalDate.now();
        when(rentRepository.findByCarAndEndDateGreaterThanEqual(eq(testCar), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // When
        rentService.getActiveOrUpcomingRentsForCar(testCar);

        // Then
        verify(rentRepository).findByCarAndEndDateGreaterThanEqual(eq(testCar), eq(expectedDate));
    }

    // ===== EDGE CASE TESTS =====
    @Test
    void addRent_WhenCarIdIsNull_ShouldThrowIllegalArgumentException() {
        // Given
        // Use reflection to set car ID to null since setId doesn't exist
        try {
            java.lang.reflect.Field carIdField = Car.class.getDeclaredField("id");
            carIdField.setAccessible(true);
            carIdField.set(testRent.getCar(), null);
        } catch (Exception e) {
            // Create new car with null ID for this test
            Car carWithNullId = new Car();
            testRent.setCar(carWithNullId);
        }

        // When & Then
        assertThatThrownBy(() -> rentService.addRent(testRent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car with ID null does not exist.");

        verify(carRepository).findById(null);
        verify(rentRepository, never()).save(any());
    }

    // ===== HELPER METHODS =====
    private Car createTestCar() {
        Car car = new Car();
        // Use reflection to set ID for testing
        try {
            java.lang.reflect.Field idField = Car.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(car, 1L);
        } catch (Exception e) {
            // ID setting failed, but that's okay for unit tests
        }
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setLicensePlate("ABC-123");
        car.setOwnerEmail("owner@example.com");
        car.setType(CarType.SEDAN);
        car.setNumberOfSeats(5);
        car.setAvailableForRent(true);
        return car;
    }

    private Rent createTestRent() {
        Rent rent = new Rent();
        // Use reflection to set ID for testing
        try {
            java.lang.reflect.Field idField = Rent.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rent, 1L);
        } catch (Exception e) {
            // ID setting failed, but that's okay for unit tests
        }

        rent.setCar(testCar);
        rent.setStartDate(LocalDate.now().plusDays(1));
        rent.setEndDate(LocalDate.now().plusDays(5));
        rent.setOwnerEmail("owner@example.com");
        rent.setRenterEmail("renter@example.com");

        // Create test RenterInfo
        RenterInfo renterInfo = createTestRenterInfo();
        rent.setRenterInfo(renterInfo);

        return rent;
    }

    private RenterInfo createTestRenterInfo() {
        RenterInfo renterInfo = new RenterInfo();
        renterInfo.setPhoneNumber("0123456789");
        renterInfo.setNationalRegisterId("85.01.01-123.45");
        renterInfo.setBirthDate(LocalDate.of(1985, 1, 1));
        renterInfo.setDrivingLicenseNumber("1234567890");
        return renterInfo;
    }
}