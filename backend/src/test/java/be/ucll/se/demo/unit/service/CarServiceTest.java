package be.ucll.se.demo.unit.service;

import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.mapper.CarMapper;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.service.CarService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarService carService;

    private Car testCar;
    private CarDTO testCarDTO;
    private CarCreateDTO testCarCreateDTO;

    @BeforeEach
    void setUp() {
        testCar = createTestCar();
        testCarDTO = createTestCarDTO();
        testCarCreateDTO = createTestCarCreateDTO();
    }

    // ===== GET ALL CARS TESTS =====
    @Test
    void getAllCarsDTO_ShouldReturnListOfCarDTOs() {
        // Given
        List<Car> cars = Arrays.asList(testCar, createTestCar());
        when(carRepository.findAll()).thenReturn(cars);
        when(carMapper.toDTO(any(Car.class))).thenReturn(testCarDTO);

        // When
        List<CarDTO> result = carService.getAllCarsDTO();

        // Then
        assertThat(result).hasSize(2);
        verify(carRepository).findAll();
        verify(carMapper, times(2)).toDTO(any(Car.class));
    }

    @Test
    void getAllCarsDTO_WhenNoCarsExist_ShouldReturnEmptyList() {
        // Given
        when(carRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<CarDTO> result = carService.getAllCarsDTO();

        // Then
        assertThat(result).isEmpty();
        verify(carRepository).findAll();
        verify(carMapper, never()).toDTO(any(Car.class));
    }

    // ===== GET CAR BY ID TESTS =====
    @Test
    void getCarByIdDTO_WhenCarExists_ShouldReturnCarDTO() {
        // Given
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(carMapper.toDTO(testCar)).thenReturn(testCarDTO);

        // When
        Optional<CarDTO> result = carService.getCarByIdDTO(carId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCarDTO);
        verify(carRepository).findById(carId);
        verify(carMapper).toDTO(testCar);
    }

    @Test
    void getCarByIdDTO_WhenCarDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When
        Optional<CarDTO> result = carService.getCarByIdDTO(carId);

        // Then
        assertThat(result).isEmpty();
        verify(carRepository).findById(carId);
        verify(carMapper, never()).toDTO(any(Car.class));
    }

    // ===== CREATE CAR TESTS =====
    @Test
    void createCar_ShouldCreateAndReturnCarDTO() {
        // Given
        when(carMapper.toEntity(testCarCreateDTO)).thenReturn(testCar);
        when(carRepository.save(testCar)).thenReturn(testCar);
        when(carMapper.toDTO(testCar)).thenReturn(testCarDTO);

        // When
        CarDTO result = carService.createCar(testCarCreateDTO);

        // Then
        assertThat(result).isEqualTo(testCarDTO);
        verify(carMapper).toEntity(testCarCreateDTO);
        verify(carRepository).save(testCar);
        verify(carMapper).toDTO(testCar);
    }

    // ===== UPDATE CAR TESTS =====
    @Test
    void updateCar_WhenCarExists_ShouldUpdateAndReturnCarDTO() {
        // Given
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(carRepository.save(testCar)).thenReturn(testCar);
        when(carMapper.toDTO(testCar)).thenReturn(testCarDTO);

        // When
        Optional<CarDTO> result = carService.updateCar(carId, testCarCreateDTO);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCarDTO);
        verify(carRepository).findById(carId);
        verify(carMapper).updateEntityFromDTO(testCar, testCarCreateDTO);
        verify(carRepository).save(testCar);
        verify(carMapper).toDTO(testCar);
    }

    @Test
    void updateCar_WhenCarDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When
        Optional<CarDTO> result = carService.updateCar(carId, testCarCreateDTO);

        // Then
        assertThat(result).isEmpty();
        verify(carRepository).findById(carId);
        verify(carMapper, never()).updateEntityFromDTO(any(), any());
        verify(carRepository, never()).save(any());
    }

    // ===== DELETE CAR TESTS =====
    @Test
    void deleteCar_WhenCarExists_ShouldDeleteAndReturnTrue() {
        // Given
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);

        // When
        boolean result = carService.deleteCar(carId);

        // Then
        assertThat(result).isTrue();
        verify(carRepository).existsById(carId);
        verify(carRepository).deleteById(carId);
    }

    @Test
    void deleteCar_WhenCarDoesNotExist_ShouldReturnFalse() {
        // Given
        Long carId = 999L;
        when(carRepository.existsById(carId)).thenReturn(false);

        // When
        boolean result = carService.deleteCar(carId);

        // Then
        assertThat(result).isFalse();
        verify(carRepository).existsById(carId);
        verify(carRepository, never()).deleteById(anyLong());
    }

    // ===== GET CAR BY LICENSE PLATE TESTS =====
    @Test
    void getCarByLicensePlateDTO_WhenCarExists_ShouldReturnCarDTO() {
        // Given
        String licensePlate = "ABC-123";
        when(carRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(testCar));
        when(carMapper.toDTO(testCar)).thenReturn(testCarDTO);

        // When
        Optional<CarDTO> result = carService.getCarByLicensePlateDTO(licensePlate);

        // Then
        assertThat(result).isPresent();
        verify(carRepository).findByLicensePlate(licensePlate);
        verify(carMapper).toDTO(testCar);
    }

    // ===== GET CARS BY OWNER EMAIL TESTS =====
    @Test
    void getCarsByOwnerEmailDTO_ShouldReturnCarDTOList() {
        // Given
        String email = "test@example.com";
        List<Car> cars = Arrays.asList(testCar);
        when(carRepository.findByOwnerEmail(email)).thenReturn(cars);
        when(carMapper.toDTO(any(Car.class))).thenReturn(testCarDTO);

        // When
        List<CarDTO> result = carService.getCarsByOwnerEmailDTO(email);

        // Then
        assertThat(result).hasSize(1);
        verify(carRepository).findByOwnerEmail(email);
        verify(carMapper).toDTO(testCar);
    }

    // ===== GET AVAILABLE CARS TESTS =====
    @Test
    void getAvailableCarsDTO_ShouldReturnAvailableCarDTOs() {
        // Given
        List<Car> availableCars = Arrays.asList(testCar);
        when(carRepository.findByAvailableForRentTrue()).thenReturn(availableCars);
        when(carMapper.toDTO(any(Car.class))).thenReturn(testCarDTO);

        // When
        List<CarDTO> result = carService.getAvailableCarsDTO();

        // Then
        assertThat(result).hasSize(1);
        verify(carRepository).findByAvailableForRentTrue();
        verify(carMapper).toDTO(testCar);
    }

    // ===== GET CARS BY TYPE TESTS =====
    @Test
    void getCarsByTypeDTO_WithValidType_ShouldReturnCarDTOs() {
        // Given
        String type = "sedan";
        CarType carType = CarType.SEDAN;
        List<Car> cars = Arrays.asList(testCar);
        when(carRepository.findByType(carType)).thenReturn(cars);
        when(carMapper.toDTO(any(Car.class))).thenReturn(testCarDTO);

        // When
        List<CarDTO> result = carService.getCarsByTypeDTO(type);

        // Then
        assertThat(result).hasSize(1);
        verify(carRepository).findByType(carType);
        verify(carMapper).toDTO(testCar);
    }

    @Test
    void getCarsByTypeDTO_WithInvalidType_ShouldThrowIllegalArgumentException() {
        // Given
        String invalidType = "INVALID_TYPE";

        // When & Then
        assertThatThrownBy(() -> carService.getCarsByTypeDTO(invalidType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid car type: " + invalidType);

        verify(carRepository, never()).findByType(any());
    }

    @Test
    void getCarsByTypeDTO_WithLowercaseType_ShouldWorkCorrectly() {
        // Given
        String type = "suv";
        CarType carType = CarType.SUV;
        List<Car> cars = Arrays.asList(testCar);
        when(carRepository.findByType(carType)).thenReturn(cars);
        when(carMapper.toDTO(any(Car.class))).thenReturn(testCarDTO);

        // When
        List<CarDTO> result = carService.getCarsByTypeDTO(type);

        // Then
        assertThat(result).hasSize(1);
        verify(carRepository).findByType(carType);
    }

    // ===== LEGACY METHOD TESTS =====
    @Test
    void getAllCars_ShouldReturnListOfCars() {
        // Given
        List<Car> cars = Arrays.asList(testCar);
        when(carRepository.findAll()).thenReturn(cars);

        // When
        List<Car> result = carService.getAllCars();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCar);
        verify(carRepository).findAll();
    }

    @Test
    void addCar_ShouldSaveAndReturnCar() {
        // Given
        when(carMapper.toEntity(testCarCreateDTO)).thenReturn(testCar);
        when(carRepository.save(testCar)).thenReturn(testCar);

        // When
        Car result = carService.addCar(testCarCreateDTO);

        // Then
        assertThat(result).isEqualTo(testCar);
        verify(carMapper).toEntity(testCarCreateDTO);
        verify(carRepository).save(testCar);
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

    private CarDTO createTestCarDTO() {
        CarDTO dto = new CarDTO();
        dto.setId(1L); // CarDTO has setId method
        dto.setBrand("Toyota");
        dto.setModel("Camry");
        dto.setLicensePlate("ABC-123");
        dto.setOwnerEmail("owner@example.com");
        dto.setType(CarType.SEDAN);
        dto.setNumberOfSeats(5);
        dto.setAvailableForRent(true);
        return dto;
    }

    private CarCreateDTO createTestCarCreateDTO() {
        CarCreateDTO dto = new CarCreateDTO();
        dto.setBrand("Toyota");
        dto.setModel("Camry");
        dto.setLicensePlate("ABC-123");
        dto.setType(CarType.SEDAN);
        dto.setAvailableForRent(true);
        return dto;
    }
}