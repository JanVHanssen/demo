package be.ucll.se.demo.bdd.steps;

import be.ucll.se.demo.dto.*;
import be.ucll.se.demo.model.*;
import be.ucll.se.demo.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarBookingSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentRepository rentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String baseUrl;
    private String currentUserToken;
    private String currentUserEmail;
    private List<CarDTO> searchResults;
    private RentDTO currentBooking;
    private ResponseEntity<?> lastResponse;
    private LocalDate bookingStartDate;
    private LocalDate bookingEndDate;
    private String renterPhoneNumber;
    private String renterNationalRegisterId;
    private LocalDate renterBirthDate;
    private String renterDrivingLicenseNumber;
    private Long selectedCarId;

    @Before
    public void setUp() {
        baseUrl = "http://localhost:" + port;

        // Clean database before each scenario
        try {
            notificationRepository.deleteAll();
            rentRepository.deleteAll();
            carRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            // Database might be empty, that's fine
        }
    }

    @Given("the following users exist:")
    public void createUsers(DataTable dataTable) {
        List<Map<String, String>> users = dataTable.asMaps();

        for (Map<String, String> userData : users) {
            // Create registration request as Map (since no UserCreateDTO exists)
            Map<String, String> registerRequest = Map.of(
                    "username", userData.get("username"),
                    "email", userData.get("email"),
                    "password", "password123",
                    "role", userData.get("role"));

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(
                        baseUrl + "/auth/register", registerRequest, String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            } catch (Exception e) {
                System.out.println("Failed to create user: " + userData.get("email") + " - " + e.getMessage());
                throw e;
            }
        }
    }

    @Given("the following cars exist:")
    public void createCars(DataTable dataTable) {
        List<Map<String, String>> cars = dataTable.asMaps();

        for (Map<String, String> carData : cars) {
            // First login as owner to get token
            String ownerToken = loginUser(carData.get("ownerEmail"), "password123");

            // Create car
            CarCreateDTO carDto = new CarCreateDTO();
            carDto.setBrand(carData.get("brand"));
            carDto.setModel(carData.get("model"));
            carDto.setLicensePlate(carData.get("licensePlate"));
            carDto.setOwnerEmail(carData.get("ownerEmail"));
            carDto.setType(CarType.valueOf(carData.get("type")));
            carDto.setNumberOfSeats(Integer.parseInt(carData.get("seats")));
            carDto.setAvailableForRent(Boolean.parseBoolean(carData.get("available")));

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(ownerToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CarCreateDTO> request = new HttpEntity<>(carDto, headers);

            try {
                ResponseEntity<CarDTO> response = restTemplate.postForEntity(
                        baseUrl + "/cars", request, CarDTO.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            } catch (Exception e) {
                System.out.println("Failed to create car: " + carData.get("licensePlate") + " - " + e.getMessage());
                throw e;
            }
        }
    }

    @Given("I am logged in as {string}")
    public void loginAs(String email) {
        currentUserEmail = email;
        currentUserToken = loginUser(email, "password123");
        assertThat(currentUserToken).isNotNull();
    }

    @When("I search for cars from {string} to {string}")
    public void searchCars(String startDate, String endDate) {
        String url = String.format("%s/cars/available?startDate=%s&endDate=%s",
                baseUrl, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUserToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<CarDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, CarDTO[].class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            searchResults = List.of(response.getBody());
        } catch (Exception e) {
            System.out.println("Failed to search cars: " + e.getMessage());
            throw e;
        }
    }

    @Then("I should see {int} available cars")
    public void verifyCarsCount(int expectedCount) {
        assertThat(searchResults).hasSize(expectedCount);
    }

    @When("I select the {string} with license plate {string}")
    public void selectCar(String carName, String licensePlate) {
        CarDTO selectedCar = searchResults.stream()
                .filter(car -> car.getLicensePlate().equals(licensePlate))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Car not found with license plate: " + licensePlate));

        selectedCarId = selectedCar.getId();

        // Verify it's the right car
        String fullCarName = selectedCar.getBrand() + " " + selectedCar.getModel();
        assertThat(fullCarName).containsIgnoringCase(carName.split(" ")[0]); // Check brand at least
    }

    @When("I choose dates from {string} to {string}")
    public void chooseDates(String startDate, String endDate) {
        this.bookingStartDate = LocalDate.parse(startDate);
        this.bookingEndDate = LocalDate.parse(endDate);
    }

    @When("I enter my renter information:")
    public void enterRenterInfo(DataTable dataTable) {
        Map<String, String> renterData = dataTable.asMaps().get(0);

        this.renterPhoneNumber = renterData.get("phoneNumber");
        this.renterNationalRegisterId = renterData.get("nationalRegisterId");
        this.renterBirthDate = LocalDate.parse(renterData.get("birthDate"));
        this.renterDrivingLicenseNumber = renterData.get("drivingLicenseNumber");
    }

    @When("I confirm the booking")
    public void confirmBooking() {
        CarDTO selectedCar = searchResults.stream()
                .filter(car -> car.getId().equals(selectedCarId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Selected car not found"));

        RentCreateDTO rentDto = new RentCreateDTO();
        rentDto.setCarId(selectedCar.getId());
        rentDto.setStartDate(bookingStartDate);
        rentDto.setEndDate(bookingEndDate);
        rentDto.setOwnerEmail(selectedCar.getOwnerEmail());
        rentDto.setRenterEmail(currentUserEmail);
        rentDto.setPhoneNumber(renterPhoneNumber);
        rentDto.setNationalRegisterId(renterNationalRegisterId);
        rentDto.setBirthDate(renterBirthDate);
        rentDto.setDrivingLicenseNumber(renterDrivingLicenseNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUserToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RentCreateDTO> request = new HttpEntity<>(rentDto, headers);

        try {
            ResponseEntity<RentDTO> response = restTemplate.postForEntity(
                    baseUrl + "/rents", request, RentDTO.class);

            lastResponse = response;
            if (response.getStatusCode().is2xxSuccessful()) {
                currentBooking = response.getBody();
            }
        } catch (Exception e) {
            System.out.println("Failed to create booking: " + e.getMessage());
            lastResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Then("the booking should be created successfully")
    public void verifyBookingCreated() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(currentBooking).isNotNull();
        assertThat(currentBooking.getId()).isNotNull();
    }

    @Then("I should receive a booking confirmation email")
    public void verifyRenterNotification() {
        // Wait a bit for async processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify notification was created in database
        List<Notification> notifications = notificationRepository
                .findByRecipientEmailAndType(currentUserEmail, NotificationType.BOOKING_CONFIRMATION);

        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).containsIgnoringCase("bevestig");
    }

    @Then("the car owner should receive a new booking notification")
    public void verifyOwnerNotification() {
        // Wait a bit for async processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CarDTO selectedCar = searchResults.stream()
                .filter(car -> car.getId().equals(selectedCarId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Selected car not found"));

        List<Notification> notifications = notificationRepository
                .findByRecipientEmailAndType(selectedCar.getOwnerEmail(), NotificationType.NEW_BOOKING);

        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).containsIgnoringCase("boeking");
    }

    // Helper methods
    private String loginUser(String email, String password) {
        // Find username by email first
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        String username = userOpt.get().getUsername();

        // Create login request as Map (since no LoginDTO exists)
        Map<String, String> loginRequest = Map.of(
                "username", username,
                "password", password);

        try {
            ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login", loginRequest, LoginResponseDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return response.getBody().getToken();
        } catch (Exception e) {
            System.out.println("Failed to login user: " + email + " - " + e.getMessage());
            throw e;
        }
    }
}