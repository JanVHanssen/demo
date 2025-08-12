package be.ucll.se.demo.unit.service;

import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.mapper.RentalMapper;
import be.ucll.se.demo.model.Rental;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.model.PickupPoint;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.PickupPointDTO;
import be.ucll.se.demo.dto.ContactDTO;
import be.ucll.se.demo.model.Contact;
import java.time.LocalTime;
import be.ucll.se.demo.repository.RentalRepository;
import be.ucll.se.demo.service.RentalService;
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
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalService rentalService;

    private Rental testRental;
    private RentalDTO testRentalDTO;
    private RentalCreateDTO testRentalCreateDTO;
    private Car testCar;
    private PickupPoint testPickupPoint;

    @BeforeEach
    void setUp() {
        testPickupPoint = createTestPickupPoint();
        testCar = createTestCar();
        testRental = createTestRental();
        testRentalDTO = createTestRentalDTO();
        testRentalCreateDTO = createTestRentalCreateDTO();
    }

    // ===== GET ALL RENTALS TESTS =====
    @Test
    void getAllRentalsDTO_ShouldReturnListOfRentalDTOs() {
        // Given
        List<Rental> rentals = Arrays.asList(testRental, createTestRental());
        when(rentalRepository.findAll()).thenReturn(rentals);
        when(rentalMapper.toDTO(any(Rental.class))).thenReturn(testRentalDTO);

        // When
        List<RentalDTO> result = rentalService.getAllRentalsDTO();

        // Then
        assertThat(result).hasSize(2);
        verify(rentalRepository).findAll();
        verify(rentalMapper, times(2)).toDTO(any(Rental.class));
    }

    @Test
    void getAllRentalsDTO_WhenNoRentalsExist_ShouldReturnEmptyList() {
        // Given
        when(rentalRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<RentalDTO> result = rentalService.getAllRentalsDTO();

        // Then
        assertThat(result).isEmpty();
        verify(rentalRepository).findAll();
        verify(rentalMapper, never()).toDTO(any(Rental.class));
    }

    // ===== GET RENTAL BY ID TESTS =====
    @Test
    void getRentalByIdDTO_WhenRentalExists_ShouldReturnRentalDTO() {
        // Given
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(testRental));
        when(rentalMapper.toDTO(testRental)).thenReturn(testRentalDTO);

        // When
        Optional<RentalDTO> result = rentalService.getRentalByIdDTO(rentalId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRentalDTO);
        verify(rentalRepository).findById(rentalId);
        verify(rentalMapper).toDTO(testRental);
    }

    @Test
    void getRentalByIdDTO_WhenRentalDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long rentalId = 999L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        // When
        Optional<RentalDTO> result = rentalService.getRentalByIdDTO(rentalId);

        // Then
        assertThat(result).isEmpty();
        verify(rentalRepository).findById(rentalId);
        verify(rentalMapper, never()).toDTO(any(Rental.class));
    }

    // ===== CREATE RENTAL TESTS =====
    @Test
    void createRental_WhenCarExists_ShouldCreateAndReturnRentalDTO() {
        // Given
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(rentalMapper.toEntity(testRentalCreateDTO, testCar)).thenReturn(testRental);
        when(rentalRepository.save(testRental)).thenReturn(testRental);
        when(rentalMapper.toDTO(testRental)).thenReturn(testRentalDTO);

        // When
        RentalDTO result = rentalService.createRental(testRentalCreateDTO);

        // Then
        assertThat(result).isEqualTo(testRentalDTO);
        verify(carRepository).findById(carId);
        verify(rentalMapper).toEntity(testRentalCreateDTO, testCar);
        verify(rentalRepository).save(testRental);
        verify(rentalMapper).toDTO(testRental);
    }

    @Test
    void createRental_WhenCarDoesNotExist_ShouldThrowIllegalArgumentException() {
        // Given
        Long nonExistentCarId = 999L;
        testRentalCreateDTO.setCarId(nonExistentCarId);
        when(carRepository.findById(nonExistentCarId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rentalService.createRental(testRentalCreateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car not found with id: " + nonExistentCarId);

        verify(carRepository).findById(nonExistentCarId);
        verify(rentalMapper, never()).toEntity(any(), any());
        verify(rentalRepository, never()).save(any());
    }

    // ===== UPDATE RENTAL TESTS =====
    @Test
    void updateRental_WhenRentalAndCarExist_ShouldUpdateAndReturnRentalDTO() {
        // Given
        Long rentalId = 1L;
        Long carId = 1L;
        Rental updatedRental = createTestRental();

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(testRental));
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(rentalMapper.toEntity(testRentalCreateDTO, testCar)).thenReturn(updatedRental);
        when(rentalRepository.save(any(Rental.class))).thenReturn(updatedRental);
        when(rentalMapper.toDTO(updatedRental)).thenReturn(testRentalDTO);

        // When
        Optional<RentalDTO> result = rentalService.updateRental(rentalId, testRentalCreateDTO);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRentalDTO);
        verify(rentalRepository).findById(rentalId);
        verify(carRepository).findById(carId);
        verify(rentalMapper).toEntity(testRentalCreateDTO, testCar);
        verify(rentalRepository).save(any(Rental.class));
        verify(rentalMapper).toDTO(updatedRental);
    }

    @Test
    void updateRental_WhenRentalDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long nonExistentRentalId = 999L;
        when(rentalRepository.findById(nonExistentRentalId)).thenReturn(Optional.empty());

        // When
        Optional<RentalDTO> result = rentalService.updateRental(nonExistentRentalId, testRentalCreateDTO);

        // Then
        assertThat(result).isEmpty();
        verify(rentalRepository).findById(nonExistentRentalId);
        verify(carRepository, never()).findById(anyLong());
        verify(rentalMapper, never()).toEntity(any(), any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void updateRental_WhenRentalExistsButCarDoesNot_ShouldThrowIllegalArgumentException() {
        // Given
        Long rentalId = 1L;
        Long nonExistentCarId = 999L;
        testRentalCreateDTO.setCarId(nonExistentCarId);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(testRental));
        when(carRepository.findById(nonExistentCarId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rentalService.updateRental(rentalId, testRentalCreateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car not found with id: " + nonExistentCarId);

        verify(rentalRepository).findById(rentalId);
        verify(carRepository).findById(nonExistentCarId);
        verify(rentalMapper, never()).toEntity(any(), any());
        verify(rentalRepository, never()).save(any());
    }

    // ===== DELETE RENTAL TESTS =====
    @Test
    void deleteRental_WhenRentalExists_ShouldDeleteAndReturnTrue() {
        // Given
        Long rentalId = 1L;
        when(rentalRepository.existsById(rentalId)).thenReturn(true);

        // When
        boolean result = rentalService.deleteRental(rentalId);

        // Then
        assertThat(result).isTrue();
        verify(rentalRepository).existsById(rentalId);
        verify(rentalRepository).deleteById(rentalId);
    }

    @Test
    void deleteRental_WhenRentalDoesNotExist_ShouldReturnFalse() {
        // Given
        Long rentalId = 999L;
        when(rentalRepository.existsById(rentalId)).thenReturn(false);

        // When
        boolean result = rentalService.deleteRental(rentalId);

        // Then
        assertThat(result).isFalse();
        verify(rentalRepository).existsById(rentalId);
        verify(rentalRepository, never()).deleteById(anyLong());
    }

    // ===== GET RENTALS BY CAR ID TESTS =====
    @Test
    void getRentalsByCarIdDTO_ShouldReturnRentalDTOList() {
        // Given
        Long carId = 1L;
        List<Rental> rentals = Arrays.asList(testRental);
        when(rentalRepository.findByCarId(carId)).thenReturn(rentals);
        when(rentalMapper.toDTO(any(Rental.class))).thenReturn(testRentalDTO);

        // When
        List<RentalDTO> result = rentalService.getRentalsByCarIdDTO(carId);

        // Then
        assertThat(result).hasSize(1);
        verify(rentalRepository).findByCarId(carId);
        verify(rentalMapper).toDTO(testRental);
    }

    // ===== GET RENTALS BY OWNER EMAIL TESTS =====
    @Test
    void getRentalsByOwnerEmailDTO_ShouldReturnRentalDTOList() {
        // Given
        String email = "test@example.com";
        List<Rental> rentals = Arrays.asList(testRental);
        when(rentalRepository.findByOwnerEmail(email)).thenReturn(rentals);
        when(rentalMapper.toDTO(any(Rental.class))).thenReturn(testRentalDTO);

        // When
        List<RentalDTO> result = rentalService.getRentalsByOwnerEmailDTO(email);

        // Then
        assertThat(result).hasSize(1);
        verify(rentalRepository).findByOwnerEmail(email);
        verify(rentalMapper).toDTO(testRental);
    }

    // ===== GET RENTALS BY START DATE TESTS =====
    @Test
    void getRentalsByStartDateDTO_ShouldReturnRentalDTOList() {
        // Given
        LocalDate startDate = LocalDate.now();
        List<Rental> rentals = Arrays.asList(testRental);
        when(rentalRepository.findByStartDate(startDate)).thenReturn(rentals);
        when(rentalMapper.toDTO(any(Rental.class))).thenReturn(testRentalDTO);

        // When
        List<RentalDTO> result = rentalService.getRentalsByStartDateDTO(startDate);

        // Then
        assertThat(result).hasSize(1);
        verify(rentalRepository).findByStartDate(startDate);
        verify(rentalMapper).toDTO(testRental);
    }

    // ===== GET RENTALS BY CITY TESTS =====
    @Test
    void getRentalsByCityDTO_ShouldReturnRentalDTOList() {
        // Given
        String city = "Brussels";
        List<Rental> rentals = Arrays.asList(testRental);
        when(rentalRepository.findByPickupPoint_City(city)).thenReturn(rentals);
        when(rentalMapper.toDTO(any(Rental.class))).thenReturn(testRentalDTO);

        // When
        List<RentalDTO> result = rentalService.getRentalsByCityDTO(city);

        // Then
        assertThat(result).hasSize(1);
        verify(rentalRepository).findByPickupPoint_City(city);
        verify(rentalMapper).toDTO(testRental);
    }

    // ===== LEGACY METHOD TESTS =====
    @Test
    void getAllRentals_ShouldReturnListOfRentals() {
        // Given
        List<Rental> rentals = Arrays.asList(testRental);
        when(rentalRepository.findAll()).thenReturn(rentals);

        // When
        List<Rental> result = rentalService.getAllRentals();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRental);
        verify(rentalRepository).findAll();
    }

    @Test
    void getRentalById_WhenExists_ShouldReturnRental() {
        // Given
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(testRental));

        // When
        Optional<Rental> result = rentalService.getRentalById(rentalId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRental);
        verify(rentalRepository).findById(rentalId);
    }

    @Test
    void addRental_ShouldSaveAndReturnRental() {
        // Given
        when(rentalRepository.save(testRental)).thenReturn(testRental);

        // When
        Rental result = rentalService.addRental(testRental);

        // Then
        assertThat(result).isEqualTo(testRental);
        verify(rentalRepository).save(testRental);
    }

    @Test
    void getRentalsByCarId_ShouldReturnRentalList() {
        // Given
        Long carId = 1L;
        List<Rental> rentals = Arrays.asList(testRental);
        when(rentalRepository.findByCarId(carId)).thenReturn(rentals);

        // When
        List<Rental> result = rentalService.getRentalsByCarId(carId);

        // Then
        assertThat(result).hasSize(1);
        verify(rentalRepository).findByCarId(carId);
    }

    // ===== HELPER METHODS =====
    private Contact createTestContact() {
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setPhoneNumber("+32123456789"); // Note: setPhoneNumber instead of setPhone
        contact.setEmail("john@example.com");
        return contact;
    }

    private PickupPoint createTestPickupPoint() {
        PickupPoint pickupPoint = new PickupPoint();
        pickupPoint.setStreet("Station Street");
        pickupPoint.setNumber("1");
        pickupPoint.setPostal("1000");
        pickupPoint.setCity("Brussels");
        return pickupPoint;
    }

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

    private Rental createTestRental() {
        Rental rental = new Rental();
        // Use reflection to set ID for testing
        try {
            java.lang.reflect.Field idField = Rental.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rental, 1L);
        } catch (Exception e) {
            // ID setting failed, but that's okay for unit tests
        }
        rental.setCar(testCar);
        rental.setOwnerEmail("test@example.com");
        rental.setStartDate(LocalDate.now());
        rental.setStartTime(LocalTime.of(10, 0));
        rental.setEndDate(LocalDate.now().plusDays(3));
        rental.setEndTime(LocalTime.of(18, 0));
        rental.setPickupPoint(testPickupPoint);

        // Create test Contact
        Contact testContact = createTestContact();
        rental.setContact(testContact);

        return rental;
    }

    private RentalDTO createTestRentalDTO() {
        RentalDTO dto = new RentalDTO();
        try {
            java.lang.reflect.Field idField = RentalDTO.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(dto, 1L);
        } catch (Exception e) {
            // ID setting failed, but that's okay
        }

        // Create a test CarDTO
        CarDTO carDTO = new CarDTO();
        carDTO.setId(1L);
        carDTO.setBrand("Toyota");
        carDTO.setModel("Camry");
        carDTO.setLicensePlate("ABC-123");
        carDTO.setOwnerEmail("owner@example.com");
        carDTO.setType(CarType.SEDAN);
        carDTO.setNumberOfSeats(5);
        carDTO.setAvailableForRent(true);

        dto.setCar(carDTO);
        dto.setOwnerEmail("test@example.com");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(3));

        // Create test PickupPointDTO
        PickupPointDTO pickupPointDTO = new PickupPointDTO();
        pickupPointDTO.setStreet("Station Street");
        pickupPointDTO.setNumber("1");
        pickupPointDTO.setPostal("1000");
        pickupPointDTO.setCity("Brussels");
        dto.setPickupPoint(pickupPointDTO);

        // Create test ContactDTO
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setName("John Doe");
        contactDTO.setPhone("+32123456789");
        contactDTO.setEmail("john@example.com");
        dto.setContact(contactDTO);

        return dto;
    }

    private RentalCreateDTO createTestRentalCreateDTO() {
        RentalCreateDTO dto = new RentalCreateDTO();
        dto.setCarId(1L);
        dto.setOwnerEmail("test@example.com");
        dto.setStartDate("2024-01-15"); // String format as per your DTO
        dto.setStartTime("10:00");
        dto.setEndDate("2024-01-18");
        dto.setEndTime("18:00");

        // PickupPoint fields (embedded in CreateDTO)
        dto.setStreet("Station Street");
        dto.setNumber("1");
        dto.setPostal("1000");
        dto.setCity("Brussels");

        // Contact fields
        dto.setContactName("John Doe");
        dto.setPhone("+32123456789");
        dto.setEmail("john@example.com");

        return dto;
    }
}