package be.ucll.se.demo.integration;

import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.repository.UserRepository;
import be.ucll.se.demo.repository.NotificationRepository; // ← TOEGEVOEGD
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

    @Autowired
    private NotificationRepository notificationRepository; // ← TOEGEVOEGD

    private ObjectMapper objectMapper;
    private String baseUrl;
    private String ownerToken;
    private final String ownerEmail = "carowner@example.com";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // FIXED: Clean database in correct order
        try {
            // Child records first to avoid foreign key constraints
            notificationRepository.deleteAll(); // ← NIEUW: notifications eerst
            rentRepository.deleteAll(); // Then rents
            carRepository.deleteAll(); // Then cars
            userRepository.deleteAll(); // Users last

            notificationRepository.flush();
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

    // Rest of your tests stay exactly the same...
    @Test
    @Order(1)
    void createCar_ShouldSuccessfullyCreateCar() {
        System.out.println("\n🧪 TEST: Create Car");

        CarDTO createdCar = createCar("Toyota", "Camry", "ABC-123", CarType.SEDAN, 5);

        assertThat(createdCar).isNotNull();
        assertThat(createdCar.getId()).isNotNull();
        assertThat(createdCar.getBrand()).isEqualTo("Toyota");
        assertThat(createdCar.getModel()).isEqualTo("Camry");
        assertThat(createdCar.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(createdCar.getOwnerEmail()).isEqualTo(ownerEmail);
        assertThat(createdCar.getType()).isEqualTo(CarType.SEDAN);
        assertThat(createdCar.getNumberOfSeats()).isEqualTo(5);
        assertThat(createdCar.isAvailableForRent()).isTrue();

        assertThat(carRepository.count()).isEqualTo(1);

        System.out.println("✅ Car created successfully with ID: " + createdCar.getId());
    }

    // Add all your other test methods here - they stay the same!
    // Just keeping the helper methods:

    private String registerAndLogin(String username, String email, String password) {
        String uniqueUsername = username + "_" + System.currentTimeMillis() % 10000;
        String uniqueEmail = email.replace("@", "_" + System.currentTimeMillis() % 10000 + "@");

        Map<String, String> registerBody = Map.of(
                "username", uniqueUsername,
                "email", uniqueEmail,
                "password", password);

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register", registerBody, Map.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

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