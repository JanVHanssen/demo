package be.ucll.se.demo.unit.service;

import be.ucll.se.demo.model.RenterInfo;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.service.RentService;
import be.ucll.se.demo.service.NotificationService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentServiceTest {

    @Mock
    private RentRepository rentRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentService rentService;

    private Rent testRent;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testCar = createTestCar(1L);
        testRent = createTestRent(1L, testCar);
    }

    // ===== GET ALL RENTS TESTS =====
    @Test
    void getAllRents_ShouldReturnListOfRents() {
        List<Rent> rents = Arrays.asList(testRent, createTestRent(2L, testCar));
        when(rentRepository.findAll()).thenReturn(rents);

        List<Rent> result = rentService.getAllRents();

        assertThat(result).hasSize(2);
        verify(rentRepository).findAll();
    }

    @Test
    void getAllRents_WhenNoRentsExist_ShouldReturnEmptyList() {
        when(rentRepository.findAll()).thenReturn(List.of());

        List<Rent> result = rentService.getAllRents();

        assertThat(result).isEmpty();
        verify(rentRepository).findAll();
    }

    // ===== GET RENT BY ID TESTS =====
    @Test
    void getRentById_WhenRentExists_ShouldReturnRent() {
        Long rentId = 1L;
        when(rentRepository.findById(rentId)).thenReturn(Optional.of(testRent));

        Optional<Rent> result = rentService.getRentById(rentId);

        assertThat(result).isPresent().contains(testRent);
        verify(rentRepository).findById(rentId);
    }

    @Test
    void getRentById_WhenRentDoesNotExist_ShouldReturnEmpty() {
        Long rentId = 999L;
        when(rentRepository.findById(rentId)).thenReturn(Optional.empty());

        Optional<Rent> result = rentService.getRentById(rentId);

        assertThat(result).isEmpty();
        verify(rentRepository).findById(rentId);
    }

    // ===== ADD RENT TESTS =====
    @Test
    void addRent_WhenValidRent_ShouldSaveAndReturnRent() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(rentRepository.save(testRent)).thenReturn(testRent);

        Rent result = rentService.addRent(testRent);

        assertThat(result).isEqualTo(testRent);
        verify(carRepository).findById(1L);
        verify(rentRepository).save(testRent);
        verify(notificationService).notifyOwnerOfNewBooking(testRent);
        verify(notificationService).notifyRenterOfConfirmation(testRent);
    }

    @Test
    void addRent_WhenCarDoesNotExist_ShouldThrowIllegalArgumentException() {
        Car nonExistentCar = createTestCar(999L);
        testRent.setCar(nonExistentCar);

        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentService.addRent(testRent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car with ID 999 does not exist.");

        verify(carRepository).findById(999L);
        verify(rentRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void addRent_WhenStartDateAfterEndDate_ShouldThrowIllegalArgumentException() {
        testRent.setStartDate(LocalDate.now().plusDays(5));
        testRent.setEndDate(LocalDate.now().plusDays(2));
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));

        assertThatThrownBy(() -> rentService.addRent(testRent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be before end date.");

        verify(carRepository).findById(1L);
        verify(rentRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void addRent_WhenStartDateEqualsEndDate_ShouldNotThrowException() {
        LocalDate sameDate = LocalDate.now().plusDays(1);
        testRent.setStartDate(sameDate);
        testRent.setEndDate(sameDate);
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(rentRepository.save(testRent)).thenReturn(testRent);

        Rent result = rentService.addRent(testRent);

        assertThat(result).isEqualTo(testRent);
        verify(carRepository).findById(1L);
        verify(rentRepository).save(testRent);
        verify(notificationService).notifyOwnerOfNewBooking(testRent);
        verify(notificationService).notifyRenterOfConfirmation(testRent);
    }

    // ===== DELETE RENT TESTS =====
    @Test
    void deleteRent_WhenRentExists_ShouldDeleteAndSendNotifications() {
        when(rentRepository.findById(1L)).thenReturn(Optional.of(testRent));

        rentService.deleteRent(1L);

        verify(rentRepository).findById(1L);
        verify(notificationService).notifyBookingCancellation(testRent);
        verify(rentRepository).deleteById(1L);
    }

    @Test
    void deleteRent_WhenRentDoesNotExist_ShouldThrowException() {
        when(rentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentService.deleteRent(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rent with ID 999 does not exist.");

        verify(rentRepository).findById(999L);
        verifyNoInteractions(notificationService);
        verify(rentRepository, never()).deleteById(anyLong());
    }

    // ===== HELPER METHODS =====
    private Car createTestCar(Long id) {
        Car car = new Car();
        car.setId(id); // 👉 stel gewoon rechtstreeks in (voeg setter toe aan Car als die er nog niet
                       // is)
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setLicensePlate("ABC-123");
        car.setOwnerEmail("owner@example.com");
        car.setType(CarType.SEDAN);
        car.setNumberOfSeats(5);
        car.setAvailableForRent(true);
        return car;
    }

    private Rent createTestRent(Long id, Car car) {
        Rent rent = new Rent();
        rent.setId(id); // 👉 idem hier, zet gewoon een setter in Rent
        rent.setCar(car);
        rent.setStartDate(LocalDate.now().plusDays(1));
        rent.setEndDate(LocalDate.now().plusDays(5));
        rent.setOwnerEmail("owner@example.com");
        rent.setRenterEmail("renter@example.com");

        RenterInfo renterInfo = new RenterInfo();
        renterInfo.setPhoneNumber("0123456789");
        renterInfo.setNationalRegisterId("85.01.01-123.45");
        renterInfo.setBirthDate(LocalDate.of(1985, 1, 1));
        renterInfo.setDrivingLicenseNumber("1234567890");
        rent.setRenterInfo(renterInfo);

        return rent;
    }
}
