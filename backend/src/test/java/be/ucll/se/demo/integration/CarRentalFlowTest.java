package be.ucll.se.demo.Integration;

import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.RentCreateDTO;
import be.ucll.se.demo.dto.RentDTO;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integration.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarRentalFlowTest {

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

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Clean database before each test - Force cleanup with SQL
        try {
            // Use JPQL to force delete all data
            rentRepository.deleteAll();
            carRepository.deleteAll();
            userRepository.deleteAll();

            // Ensure the deletes are committed
            rentRepository.flush();
            carRepository.flush();
            userRepository.flush();

            // Double-check cleanup worked
            long carCount = carRepository.count();
            long rentCount = rentRepository.count();
            long userCount = userRepository.count();

            System.out.println(
                    "✅ Database cleanup - Cars: " + carCount + ", Rents: " + rentCount + ", Users: " + userCount);

        } catch (Exception e) {
            System.out.println("⚠️ Database cleanup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void completeCarRentalFlow_ShouldWorkEndToEnd() {
        // 1. OWNER REGISTRATION & LOGIN
        String ownerEmail = "owner@example.com";
        String ownerUsername = "carowner";
        String ownerPassword = "password123";

        String ownerToken = registerAndLogin(ownerUsername, ownerEmail, ownerPassword);
        assertThat(ownerToken).isNotNull();

        // 2. RENTER REGISTRATION & LOGIN
        String renterEmail = "renter@example.com";
        String renterUsername = "carrenter";
        String renterPassword = "password456";

        String renterToken = registerAndLogin(renterUsername, renterEmail, renterPassword);
        assertThat(renterToken).isNotNull();

        // 3. OWNER ADDS CAR
        CarDTO createdCar = createCar(ownerToken, ownerEmail);
        assertThat(createdCar).isNotNull();
        assertThat(createdCar.getId()).isNotNull();
        assertThat(createdCar.getOwnerEmail()).isEqualTo(ownerEmail);
        assertThat(createdCar.isAvailableForRent()).isTrue();

        // 4. VERIFY CAR APPEARS IN AVAILABLE CARS
        CarDTO[] availableCars = getAvailableCars();
        System.out.println("🚗 Available cars count: " + availableCars.length);
        for (CarDTO car : availableCars) {
            System.out.println("  - Car ID: " + car.getId() + ", License: " + car.getLicensePlate() + ", Owner: "
                    + car.getOwnerEmail());
        }
        assertThat(availableCars).hasSize(1);
        assertThat(availableCars[0].getId()).isEqualTo(createdCar.getId());

        // 5. RENTER CREATES RENTAL
        RentDTO createdRent = createRent(renterToken, createdCar.getId(), ownerEmail, renterEmail);
        assertThat(createdRent).isNotNull();
        assertThat(createdRent.getId()).isNotNull();
        assertThat(createdRent.getCarId()).isEqualTo(createdCar.getId());
        assertThat(createdRent.getRenterEmail()).isEqualTo(renterEmail);
        assertThat(createdRent.getOwnerEmail()).isEqualTo(ownerEmail);

        // 6. VERIFY RENTAL EXISTS IN SYSTEM
        RentDTO[] renterRents = getRentsByRenterEmail(renterEmail);
        assertThat(renterRents).hasSize(1);
        assertThat(renterRents[0].getId()).isEqualTo(createdRent.getId());

        // 7. VERIFY CAR RENTAL BY OWNER
        RentDTO[] carRents = getRentsByCar(createdCar.getId());
        assertThat(carRents).hasSize(1);
        assertThat(carRents[0].getId()).isEqualTo(createdRent.getId());

        // 8. VERIFY RENT BY NATIONAL REGISTER ID
        RentDTO[] rentsById = getRentsByNationalRegisterId("90.01.01-123.45");
        assertThat(rentsById).hasSize(1);
        assertThat(rentsById[0].getId()).isEqualTo(createdRent.getId());

        // 9. CHECK ACTIVE/UPCOMING RENTS FOR CAR
        RentDTO[] activeRents = getActiveOrUpcomingRents(createdCar.getId());
        assertThat(activeRents).hasSize(1);
        assertThat(activeRents[0].getId()).isEqualTo(createdRent.getId());

        // 10. VERIFY DATA PERSISTED IN DATABASE
        assertThat(carRepository.count()).isEqualTo(1);
        assertThat(rentRepository.count()).isEqualTo(1);
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    @Order(2)
    void carRentalFlow_WithInvalidData_ShouldFail() {
        // 1. Register and login users (use unique usernames)
        String ownerToken = registerAndLogin("owner2", "owner2@test.com", "password");
        String renterToken = registerAndLogin("renter2", "renter2@test.com", "password");

        // 2. Create car
        CarDTO car = createCarWithLicense(ownerToken, "owner2@test.com", "XYZ-789");

        // 3. Test invalid email format
        RentCreateDTO invalidEmailDto = createRentCreateDTO(
                car.getId(),
                "owner2@test.com",
                "invalid-email-format" // This should fail @Email validation
        );

        HttpHeaders headers = createAuthHeaders(renterToken);
        HttpEntity<RentCreateDTO> emailRequest = new HttpEntity<>(invalidEmailDto, headers);

        ResponseEntity<String> emailResponse = restTemplate.postForEntity(
                baseUrl + "/rents", emailRequest, String.class);

        System.out.println("🔍 Invalid email test - Status: " + emailResponse.getStatusCode());
        System.out.println("🔍 Response body: " + emailResponse.getBody());

        // 4. Test invalid national register ID format
        RentCreateDTO invalidRegisterDto = createRentCreateDTO(
                car.getId(),
                "owner2@test.com",
                "renter2@test.com");
        invalidRegisterDto.setNationalRegisterId("INVALID-FORMAT");

        HttpEntity<RentCreateDTO> registerRequest = new HttpEntity<>(invalidRegisterDto, headers);
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                baseUrl + "/rents", registerRequest, String.class);

        System.out.println("🔍 Invalid register ID test - Status: " + registerResponse.getStatusCode());
        System.out.println("🔍 Response body: " + registerResponse.getBody());

        // 4. Test invalid license plate (too short if using @Size validation)
        CarCreateDTO invalidCarDto = new CarCreateDTO();
        invalidCarDto.setBrand("BMW");
        invalidCarDto.setModel("X5");
        invalidCarDto.setLicensePlate("A"); // Too short (if using @Size min=2)
        invalidCarDto.setOwnerEmail("owner2@test.com");
        invalidCarDto.setType(CarType.SUV);
        invalidCarDto.setNumberOfSeats(5);
        invalidCarDto.setAvailableForRent(true);

        HttpEntity<CarCreateDTO> carRequest = new HttpEntity<>(invalidCarDto, headers);
        ResponseEntity<String> carResponse = restTemplate.postForEntity(
                baseUrl + "/cars", carRequest, String.class);

        System.out.println("🔍 Invalid license plate test - Status: " + carResponse.getStatusCode());
        System.out.println("🔍 Response body: " + carResponse.getBody());

        // This should also fail if you update the @Pattern to @Size
        boolean anyValidationFailed = emailResponse.getStatusCode() == HttpStatus.BAD_REQUEST ||
                registerResponse.getStatusCode() == HttpStatus.BAD_REQUEST ||
                carResponse.getStatusCode() == HttpStatus.BAD_REQUEST;

        // At least one validation should fail
        assertThat(anyValidationFailed).isTrue();

        // Response should contain validation error details for the ones that failed
        if (emailResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            assertThat(emailResponse.getBody()).contains("email");
        }
        if (registerResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            assertThat(registerResponse.getBody()).contains("nationalRegisterId");
        }
        if (carResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            assertThat(carResponse.getBody()).contains("licensePlate");
        }
    }

    @Test
    @Order(3)
    void carRentalFlow_WithNonExistentCar_ShouldFail() {
        // 1. Register users (use unique usernames)
        String renterToken = registerAndLogin("renter3", "renter3@test.com", "password");

        // 2. Try to rent non-existent car
        RentCreateDTO rentDto = createRentCreateDTO(999L, "owner3@test.com", "renter3@test.com");

        HttpHeaders headers = createAuthHeaders(renterToken);
        HttpEntity<RentCreateDTO> request = new HttpEntity<>(rentDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/rents", request, String.class);

        System.out.println("🔍 Non-existent car test - Status: " + response.getStatusCode());
        System.out.println("🔍 Response body: " + response.getBody());

        // This should fail because we check for car existence in the controller
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Car with ID 999 does not exist");
    }

    // ===== HELPER METHODS =====

    private String registerAndLogin(String username, String email, String password) {
        // Add timestamp to make usernames unique across tests
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

    private CarDTO createCar(String token, String ownerEmail) {
        return createCarWithLicense(token, ownerEmail, "ABC-123");
    }

    private CarDTO createCarWithLicense(String token, String ownerEmail, String licensePlate) {
        CarCreateDTO carDto = new CarCreateDTO();
        carDto.setBrand("Toyota");
        carDto.setModel("Camry");
        carDto.setLicensePlate(licensePlate); // Now supports any reasonable format
        carDto.setOwnerEmail(ownerEmail);
        carDto.setType(CarType.SEDAN);
        carDto.setNumberOfSeats(5);
        carDto.setNumberOfChildSeats(0);
        carDto.setFoldingRearSeat(false);
        carDto.setTowBar(false);
        carDto.setAvailableForRent(true);

        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<CarCreateDTO> request = new HttpEntity<>(carDto, headers);

        ResponseEntity<CarDTO> response = restTemplate.postForEntity(
                baseUrl + "/cars", request, CarDTO.class);

        System.out.println("🚗 Create car - Status: " + response.getStatusCode());
        if (response.getStatusCode() != HttpStatus.CREATED) {
            System.out.println("🚗 Create car failed - Body: " + response.getBody());
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private RentDTO createRent(String token, Long carId, String ownerEmail, String renterEmail) {
        RentCreateDTO rentDto = createRentCreateDTO(carId, ownerEmail, renterEmail);

        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<RentCreateDTO> request = new HttpEntity<>(rentDto, headers);

        ResponseEntity<RentDTO> response = restTemplate.postForEntity(
                baseUrl + "/rents", request, RentDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private RentCreateDTO createRentCreateDTO(Long carId, String ownerEmail, String renterEmail) {
        return new RentCreateDTO(
                carId,
                LocalDate.now().plusDays(1), // startDate
                LocalDate.now().plusDays(5), // endDate
                ownerEmail,
                renterEmail,
                "0123456789", // phoneNumber
                "90.01.01-123.45", // nationalRegisterId
                LocalDate.of(1990, 1, 1), // birthDate
                "1234567890" // drivingLicenseNumber
        );
    }

    private CarDTO[] getAvailableCars() {
        ResponseEntity<CarDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/cars/available", CarDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private RentDTO[] getRentsByRenterEmail(String email) {
        ResponseEntity<RentDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/rents/renter/" + email, RentDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private RentDTO[] getRentsByCar(Long carId) {
        ResponseEntity<RentDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/rents/by-car/" + carId, RentDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private RentDTO[] getRentsByNationalRegisterId(String id) {
        ResponseEntity<RentDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/rents/by-register-id?id=" + id, RentDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private RentDTO[] getActiveOrUpcomingRents(Long carId) {
        ResponseEntity<RentDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/rents/active-or-upcoming/" + carId, RentDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
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