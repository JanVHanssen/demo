package be.ucll.se.demo.Integration;

import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integration.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarManagementFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentRepository rentRepository;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private String baseUrl;
    private String ownerToken;
    private final String ownerEmail = "carowner@example.com";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Clean database
        try {
            rentRepository.deleteAll();
            carRepository.deleteAll();
            userRepository.deleteAll();

            rentRepository.flush();
            carRepository.flush();
            userRepository.flush();

            System.out.println("✅ Database cleanup completed");
        } catch (Exception e) {
            System.out.println("⚠️ Database cleanup failed: " + e.getMessage());
        }

        // Register and login user for all tests
        ownerToken = registerAndLogin("carowner", ownerEmail, "password123");
    }

    @Test
    @Order(1)
    void createCar_ShouldSuccessfullyCreateCar() {
        System.out.println("\n🧪 TEST: Create Car");

        // Create car
        CarDTO createdCar = createCar("Toyota", "Camry", "ABC-123", CarType.SEDAN, 5);

        // Verify response
        assertThat(createdCar).isNotNull();
        assertThat(createdCar.getId()).isNotNull();
        assertThat(createdCar.getBrand()).isEqualTo("Toyota");
        assertThat(createdCar.getModel()).isEqualTo("Camry");
        assertThat(createdCar.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(createdCar.getOwnerEmail()).isEqualTo(ownerEmail);
        assertThat(createdCar.getType()).isEqualTo(CarType.SEDAN);
        assertThat(createdCar.getNumberOfSeats()).isEqualTo(5);
        assertThat(createdCar.isAvailableForRent()).isTrue();

        // Verify database persistence
        assertThat(carRepository.count()).isEqualTo(1);

        System.out.println("✅ Car created successfully with ID: " + createdCar.getId());
    }

    @Test
    @Order(2)
    void getAllCars_ShouldReturnAllCars() {
        System.out.println("\n🧪 TEST: Get All Cars");

        // Create multiple cars
        createCar("BMW", "X5", "BMW-001", CarType.SUV, 7);
        createCar("Audi", "A4", "AUDI-02", CarType.SEDAN, 5);
        createCar("Ford", "Focus", "FORD-03", CarType.HATCHBACK, 5);

        // Get all cars
        ResponseEntity<CarDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/cars", CarDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO[] cars = response.getBody();
        assertThat(cars).hasSize(3);

        // Verify car details
        assertThat(cars).extracting(CarDTO::getBrand)
                .containsExactlyInAnyOrder("BMW", "Audi", "Ford");
        assertThat(cars).extracting(CarDTO::getOwnerEmail)
                .allMatch(email -> email.equals(ownerEmail));

        System.out.println("✅ Retrieved " + cars.length + " cars successfully");
    }

    @Test
    @Order(3)
    void getCarById_ShouldReturnSpecificCar() {
        System.out.println("\n🧪 TEST: Get Car By ID");

        // Create car
        CarDTO createdCar = createCar("Mercedes", "C-Class", "MERC-01", CarType.SEDAN, 5);
        Long carId = createdCar.getId();

        // Get car by ID
        ResponseEntity<CarDTO> response = restTemplate.getForEntity(
                baseUrl + "/cars/" + carId, CarDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO retrievedCar = response.getBody();
        assertThat(retrievedCar).isNotNull();
        assertThat(retrievedCar.getId()).isEqualTo(carId);
        assertThat(retrievedCar.getBrand()).isEqualTo("Mercedes");
        assertThat(retrievedCar.getModel()).isEqualTo("C-Class");

        System.out.println("✅ Retrieved car: " + retrievedCar.getBrand() + " " + retrievedCar.getModel());
    }

    @Test
    @Order(4)
    void getCarById_WithNonExistentId_ShouldReturn404() {
        System.out.println("\n🧪 TEST: Get Non-Existent Car");

        ResponseEntity<CarDTO> response = restTemplate.getForEntity(
                baseUrl + "/cars/999", CarDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        System.out.println("✅ Non-existent car correctly returned 404");
    }

    @Test
    @Order(5)
    void updateCar_ShouldUpdateExistingCar() {
        System.out.println("\n🧪 TEST: Update Car");

        // Create car
        CarDTO createdCar = createCar("Volkswagen", "Golf", "VW-123", CarType.HATCHBACK, 5);
        Long carId = createdCar.getId();

        // Update car
        CarCreateDTO updateDto = new CarCreateDTO();
        updateDto.setBrand("Volkswagen");
        updateDto.setModel("Golf GTI"); // Updated model
        updateDto.setLicensePlate("VW-123");
        updateDto.setOwnerEmail(ownerEmail);
        updateDto.setType(CarType.HATCHBACK);
        updateDto.setNumberOfSeats(5);
        updateDto.setNumberOfChildSeats(2); // Added child seats
        updateDto.setTowBar(true); // Added tow bar
        updateDto.setAvailableForRent(false); // Changed availability

        HttpHeaders headers = createAuthHeaders(ownerToken);
        HttpEntity<CarCreateDTO> request = new HttpEntity<>(updateDto, headers);

        ResponseEntity<CarDTO> response = restTemplate.exchange(
                baseUrl + "/cars/" + carId, HttpMethod.PUT, request, CarDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO updatedCar = response.getBody();
        assertThat(updatedCar).isNotNull();
        assertThat(updatedCar.getId()).isEqualTo(carId);
        assertThat(updatedCar.getModel()).isEqualTo("Golf GTI");
        assertThat(updatedCar.getNumberOfChildSeats()).isEqualTo(2);
        assertThat(updatedCar.isTowBar()).isTrue();
        assertThat(updatedCar.isAvailableForRent()).isFalse();

        System.out.println("✅ Car updated successfully: " + updatedCar.getModel());
    }

    @Test
    @Order(6)
    void updateCar_WithNonExistentId_ShouldReturn404() {
        System.out.println("\n🧪 TEST: Update Non-Existent Car");

        CarCreateDTO updateDto = createCarCreateDTO("Test", "Car", "TEST-01", CarType.SEDAN, 5);

        HttpHeaders headers = createAuthHeaders(ownerToken);
        HttpEntity<CarCreateDTO> request = new HttpEntity<>(updateDto, headers);

        ResponseEntity<CarDTO> response = restTemplate.exchange(
                baseUrl + "/cars/999", HttpMethod.PUT, request, CarDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        System.out.println("✅ Update non-existent car correctly returned 404");
    }

    @Test
    @Order(7)
    void deleteCar_ShouldDeleteExistingCar() {
        System.out.println("\n🧪 TEST: Delete Car");

        // Create car
        CarDTO createdCar = createCar("Peugeot", "208", "PEU-123", CarType.HATCHBACK, 5);
        Long carId = createdCar.getId();

        // Verify car exists
        assertThat(carRepository.existsById(carId)).isTrue();

        // Delete car
        HttpHeaders headers = createAuthHeaders(ownerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/cars/" + carId, HttpMethod.DELETE, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify car is deleted
        assertThat(carRepository.existsById(carId)).isFalse();

        System.out.println("✅ Car deleted successfully");
    }

    @Test
    @Order(8)
    void deleteCar_WithNonExistentId_ShouldReturn404() {
        System.out.println("\n🧪 TEST: Delete Non-Existent Car");

        HttpHeaders headers = createAuthHeaders(ownerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/cars/999", HttpMethod.DELETE, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        System.out.println("✅ Delete non-existent car correctly returned 404");
    }

    @Test
    @Order(9)
    void getAvailableCars_ShouldReturnOnlyAvailableCars() {
        System.out.println("\n🧪 TEST: Get Available Cars");

        // Create available cars
        createCar("Honda", "Civic", "HON-001", CarType.SEDAN, 5, true);
        createCar("Nissan", "Qashqai", "NIS-002", CarType.SUV, 5, true);

        // Create unavailable car
        createCar("Renault", "Clio", "REN-003", CarType.HATCHBACK, 5, false);

        // Get available cars
        ResponseEntity<CarDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/cars/available", CarDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO[] availableCars = response.getBody();
        assertThat(availableCars).hasSize(2);
        assertThat(availableCars).allMatch(CarDTO::isAvailableForRent);
        assertThat(availableCars).extracting(CarDTO::getBrand)
                .containsExactlyInAnyOrder("Honda", "Nissan");

        System.out.println("✅ Retrieved " + availableCars.length + " available cars");
    }

    @Test
    @Order(10)
    void getCarsByOwnerEmail_ShouldReturnOwnerCars() {
        System.out.println("\n🧪 TEST: Get Cars By Owner Email");

        // Create cars for our owner
        createCar("Skoda", "Octavia", "SKO-001", CarType.SEDAN, 5);
        createCar("Seat", "Ibiza", "SEA-002", CarType.HATCHBACK, 5);

        // Create car for different owner (register different user)
        String otherToken = registerAndLogin("otherowner", "other@example.com", "password");
        createCarWithToken(otherToken, "Opel", "Astra", "OPE-003", CarType.HATCHBACK, 5, "other@example.com");

        // Get cars by owner email
        ResponseEntity<CarDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/cars/owner/" + ownerEmail, CarDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO[] ownerCars = response.getBody();
        assertThat(ownerCars).hasSize(2);
        assertThat(ownerCars).allMatch(car -> car.getOwnerEmail().equals(ownerEmail));
        assertThat(ownerCars).extracting(CarDTO::getBrand)
                .containsExactlyInAnyOrder("Skoda", "Seat");

        System.out.println("✅ Retrieved " + ownerCars.length + " cars for owner: " + ownerEmail);
    }

    @Test
    @Order(11)
    void getCarsByType_ShouldReturnCarsOfSpecificType() {
        System.out.println("\n🧪 TEST: Get Cars By Type");

        // Create cars of different types
        createCar("BMW", "X1", "BMW-X1", CarType.SUV, 5);
        createCar("BMW", "X3", "BMW-X3", CarType.SUV, 7);
        createCar("BMW", "320i", "BMW-320", CarType.SEDAN, 5);

        // Get SUV cars
        ResponseEntity<CarDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/cars/type/suv", CarDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO[] suvCars = response.getBody();
        assertThat(suvCars).hasSize(2);
        assertThat(suvCars).allMatch(car -> car.getType() == CarType.SUV);
        assertThat(suvCars).extracting(CarDTO::getModel)
                .containsExactlyInAnyOrder("X1", "X3");

        System.out.println("✅ Retrieved " + suvCars.length + " SUV cars");
    }

    @Test
    @Order(12)
    void getCarsByType_WithInvalidType_ShouldReturn400() {
        System.out.println("\n🧪 TEST: Get Cars By Invalid Type");

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/cars/type/INVALID_TYPE", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        System.out.println("✅ Invalid car type correctly returned 400");
    }

    @Test
    @Order(13)
    void getCarByLicensePlate_ShouldReturnCar() {
        System.out.println("\n🧪 TEST: Get Car By License Plate");

        // Create car with specific license plate
        createCar("Tesla", "Model 3", "TESLA-1", CarType.SEDAN, 5);

        // Get car by license plate
        ResponseEntity<CarDTO> response = restTemplate.getForEntity(
                baseUrl + "/cars/license/TESLA-1", CarDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarDTO car = response.getBody();
        assertThat(car).isNotNull();
        assertThat(car.getLicensePlate()).isEqualTo("TESLA-1");
        assertThat(car.getBrand()).isEqualTo("Tesla");
        assertThat(car.getModel()).isEqualTo("Model 3");

        System.out.println("✅ Retrieved car by license plate: " + car.getLicensePlate());
    }

    @Test
    @Order(14)
    void createCar_WithInvalidData_ShouldReturn400() {
        System.out.println("\n🧪 TEST: Create Car With Invalid Data");

        // Test missing required fields - but set numberOfSeats to avoid Jackson error
        CarCreateDTO invalidDto = new CarCreateDTO();
        invalidDto.setNumberOfSeats(5); // Set this to avoid null serialization error
        // Still missing brand, model, licensePlate, type which are required

        HttpHeaders headers = createAuthHeaders(ownerToken);
        HttpEntity<CarCreateDTO> request = new HttpEntity<>(invalidDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/cars", request, String.class);

        System.out.println("🔍 Response status: " + response.getStatusCode());
        System.out.println("🔍 Response body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Brand is required");

        System.out.println("✅ Invalid car data correctly returned 400");
        System.out.println("   Error: " + response.getBody());
    }

    // ===== HELPER METHODS =====

    private String registerAndLogin(String username, String email, String password) {
        // Add timestamp to make unique
        String uniqueUsername = username + "_" + System.currentTimeMillis() % 10000;
        String uniqueEmail = email.replace("@", "_" + System.currentTimeMillis() % 10000 + "@");

        // Register
        Map<String, String> registerBody = Map.of(
                "username", uniqueUsername,
                "email", uniqueEmail,
                "password", password);

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register", registerBody, Map.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Login
        Map<String, String> loginBody = Map.of(
                "username", uniqueUsername,
                "password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login", loginBody, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) loginResponse.getBody().get("token");
    }

    private CarDTO createCar(String brand, String model, String licensePlate,
            CarType type, int seats) {
        return createCar(brand, model, licensePlate, type, seats, true);
    }

    private CarDTO createCar(String brand, String model, String licensePlate,
            CarType type, int seats, boolean available) {
        return createCarWithToken(ownerToken, brand, model, licensePlate, type, seats, ownerEmail, available);
    }

    private CarDTO createCarWithToken(String token, String brand, String model, String licensePlate,
            CarType type, int seats, String email) {
        return createCarWithToken(token, brand, model, licensePlate, type, seats, email, true);
    }

    private CarDTO createCarWithToken(String token, String brand, String model, String licensePlate,
            CarType type, int seats, String email, boolean available) {
        CarCreateDTO carDto = createCarCreateDTO(brand, model, licensePlate, type, seats);
        carDto.setOwnerEmail(email);
        carDto.setAvailableForRent(available);

        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<CarCreateDTO> request = new HttpEntity<>(carDto, headers);

        ResponseEntity<CarDTO> response = restTemplate.postForEntity(
                baseUrl + "/cars", request, CarDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private CarCreateDTO createCarCreateDTO(String brand, String model, String licensePlate,
            CarType type, int seats) {
        CarCreateDTO dto = new CarCreateDTO();
        dto.setBrand(brand);
        dto.setModel(model);
        dto.setLicensePlate(licensePlate);
        dto.setOwnerEmail(ownerEmail);
        dto.setType(type);
        dto.setNumberOfSeats(seats);
        dto.setNumberOfChildSeats(0);
        dto.setFoldingRearSeat(false);
        dto.setTowBar(false);
        dto.setAvailableForRent(true);
        return dto;
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}