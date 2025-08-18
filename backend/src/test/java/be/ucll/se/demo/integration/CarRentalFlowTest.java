package be.ucll.se.demo.integration;

import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.RentCreateDTO;
import be.ucll.se.demo.dto.RentDTO;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;
import be.ucll.se.demo.repository.RentRepository;
import be.ucll.se.demo.repository.UserRepository;
import be.ucll.se.demo.repository.NotificationRepository; // ← TOEGEVOEGD
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integration.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarRentalFlowTest {

    @LocalServerPort
    private int port;

    @MockBean
    private JavaMailSender javaMailSender;

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

            long carCount = carRepository.count();
            long rentCount = rentRepository.count();
            long userCount = userRepository.count();
            long notificationCount = notificationRepository.count();

            System.out.println("✅ Database cleanup - Cars: " + carCount + ", Rents: " + rentCount +
                    ", Users: " + userCount + ", Notifications: " + notificationCount);

        } catch (Exception e) {
            System.out.println("⚠️ Database cleanup failed: " + e.getMessage());
        }
    }

    // Rest of your tests stay exactly the same...
    @Test
    @Order(1)
    void completeCarRentalFlow_ShouldWorkEndToEnd() {
        String ownerEmail = "owner@example.com";
        String ownerUsername = "carowner";
        String ownerPassword = "password123";

        String ownerToken = registerAndLogin(ownerUsername, ownerEmail, ownerPassword);
        assertThat(ownerToken).isNotNull();

        String renterEmail = "renter@example.com";
        String renterUsername = "carrenter";
        String renterPassword = "password456";

        String renterToken = registerAndLogin(renterUsername, renterEmail, renterPassword);
        assertThat(renterToken).isNotNull();

        CarDTO createdCar = createCar(ownerToken, ownerEmail);
        assertThat(createdCar).isNotNull();
        assertThat(createdCar.getId()).isNotNull();
        assertThat(createdCar.getOwnerEmail()).isEqualTo(ownerEmail);
        assertThat(createdCar.isAvailableForRent()).isTrue();

        CarDTO[] availableCars = getAvailableCars();
        System.out.println("🚗 Available cars count: " + availableCars.length);
        for (CarDTO car : availableCars) {
            System.out.println("  - Car ID: " + car.getId() + ", License: " + car.getLicensePlate() + ", Owner: "
                    + car.getOwnerEmail());
        }
        assertThat(availableCars).hasSize(1);
        assertThat(availableCars[0].getId()).isEqualTo(createdCar.getId());

        RentDTO createdRent = createRent(renterToken, createdCar.getId(), ownerEmail, renterEmail);
        assertThat(createdRent).isNotNull();
        assertThat(createdRent.getId()).isNotNull();
        assertThat(createdRent.getCarId()).isEqualTo(createdCar.getId());
        assertThat(createdRent.getRenterEmail()).isEqualTo(renterEmail);
        assertThat(createdRent.getOwnerEmail()).isEqualTo(ownerEmail);

        // Continue with verification tests...
        assertThat(carRepository.count()).isEqualTo(1);
        assertThat(rentRepository.count()).isEqualTo(1);
        assertThat(userRepository.count()).isEqualTo(2);
    }

    // Add all your helper methods - they stay the same...
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

    private CarDTO createCar(String token, String ownerEmail) {
        return createCarWithLicense(token, ownerEmail, "ABC-123");
    }

    private CarDTO createCarWithLicense(String token, String ownerEmail, String licensePlate) {
        CarCreateDTO carDto = new CarCreateDTO();
        carDto.setBrand("Toyota");
        carDto.setModel("Camry");
        carDto.setLicensePlate(licensePlate);
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
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                ownerEmail,
                renterEmail,
                "0123456789",
                "90.01.01-123.45",
                LocalDate.of(1990, 1, 1),
                "1234567890");
    }

    private CarDTO[] getAvailableCars() {
        ResponseEntity<CarDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/cars/available", CarDTO[].class);

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