// AuthenticationFlowTest.java - Updated
package be.ucll.se.demo.integration;

import be.ucll.se.demo.repository.UserRepository;
import be.ucll.se.demo.repository.NotificationRepository; // ← TOEGEVOEGD
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AuthenticationFlowTest {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

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

                // FIXED: Clean database in correct order
                try {
                        // Child records first to avoid foreign key constraints
                        notificationRepository.deleteAll(); // ← NIEUW: notifications eerst
                        userRepository.deleteAll(); // Users kunnen nu veilig verwijderd

                        notificationRepository.flush();
                        userRepository.flush();

                        System.out.println("✅ Database cleanup completed - Users: " + userRepository.count());
                } catch (Exception e) {
                        System.out.println("⚠️ Database cleanup failed: " + e.getMessage());
                }
        }

        // Rest van je tests blijven exact hetzelfde...
        @Test
        @Order(1)
        void registerUser_WithValidData_ShouldSucceed() {
                System.out.println("\n🧪 TEST: User Registration - Valid Data");

                Map<String, String> registerRequest = Map.of(
                                "username", "testuser",
                                "email", "test@example.com",
                                "password", "password123");

                ResponseEntity<Map> response = restTemplate.postForEntity(
                                baseUrl + "/auth/register", registerRequest, Map.class);

                System.out.println("🔍 Registration status: " + response.getStatusCode());
                System.out.println("🔍 Registration response: " + response.getBody());

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).containsKey("message");
                assertThat(response.getBody().get("message")).isEqualTo("Successfully registered");

                assertThat(userRepository.count()).isEqualTo(1);

                System.out.println("✅ User registered successfully");
        }

        @Test
        @Order(2)
        void registerUser_WithDuplicateUsername_ShouldFail() {
                System.out.println("\n🧪 TEST: User Registration - Duplicate Username");

                Map<String, String> firstUser = Map.of(
                                "username", "duplicateuser",
                                "email", "first@example.com",
                                "password", "password123");

                ResponseEntity<Map> firstResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register", firstUser, Map.class);
                assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                Map<String, String> secondUser = Map.of(
                                "username", "duplicateuser",
                                "email", "second@example.com",
                                "password", "password456");

                ResponseEntity<Map> secondResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register", secondUser, Map.class);

                System.out.println("🔍 Duplicate registration status: " + secondResponse.getStatusCode());
                System.out.println("🔍 Duplicate registration response: " + secondResponse.getBody());

                assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(secondResponse.getBody()).containsKey("error");
                assertThat(secondResponse.getBody().get("error")).isEqualTo("Username or email already exists");

                assertThat(userRepository.count()).isEqualTo(1);

                System.out.println("✅ Duplicate username correctly rejected");
        }

        @Test
        @Order(3)
        void registerUser_WithDuplicateEmail_ShouldFail() {
                System.out.println("\n🧪 TEST: User Registration - Duplicate Email");

                Map<String, String> firstUser = Map.of(
                                "username", "firstuser",
                                "email", "duplicate@example.com",
                                "password", "password123");

                ResponseEntity<Map> firstResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register", firstUser, Map.class);
                assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                Map<String, String> secondUser = Map.of(
                                "username", "seconduser",
                                "email", "duplicate@example.com",
                                "password", "password456");

                ResponseEntity<Map> secondResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register", secondUser, Map.class);

                System.out.println("🔍 Duplicate email status: " + secondResponse.getStatusCode());
                System.out.println("🔍 Duplicate email response: " + secondResponse.getBody());

                assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(secondResponse.getBody()).containsKey("error");

                assertThat(userRepository.count()).isEqualTo(1);

                System.out.println("✅ Duplicate email correctly rejected");
        }

        @Test
        @Order(4)
        void loginUser_WithValidCredentials_ShouldReturnToken() {
                System.out.println("\n🧪 TEST: User Login - Valid Credentials");

                Map<String, String> registerRequest = Map.of(
                                "username", "loginuser",
                                "email", "login@example.com",
                                "password", "mypassword");

                ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register", registerRequest, Map.class);
                assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                Map<String, String> loginRequest = Map.of(
                                "username", "loginuser",
                                "password", "mypassword");

                ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/login", loginRequest, Map.class);

                System.out.println("🔍 Login status: " + loginResponse.getStatusCode());
                System.out.println("🔍 Login response: " + loginResponse.getBody());

                assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(loginResponse.getBody()).containsKey("token");

                String token = (String) loginResponse.getBody().get("token");
                assertThat(token).isNotNull();
                assertThat(token).isNotEmpty();

                assertThat(loginResponse.getBody()).containsKey("userId");
                assertThat(loginResponse.getBody()).containsKey("username");
                assertThat(loginResponse.getBody()).containsKey("email");
                assertThat(loginResponse.getBody()).containsKey("roles");

                assertThat(loginResponse.getBody().get("username")).isEqualTo("loginuser");
                assertThat(loginResponse.getBody().get("email")).isEqualTo("login@example.com");

                assertThat(token.split("\\.")).hasSize(3);

                System.out.println("✅ Login successful, JWT token received");
                System.out.println("   Token preview: " + token.substring(0, Math.min(20, token.length())) + "...");
        }

        // Continue with rest of your existing tests... they stay the same
        // Just add the helper methods at the end:

        private String registerAndLogin(String username, String email, String password) {
                Map<String, String> registerRequest = Map.of(
                                "username", username,
                                "email", email,
                                "password", password);

                ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register", registerRequest, Map.class);

                assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                Map<String, String> loginRequest = Map.of(
                                "username", username,
                                "password", password);

                ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/login", loginRequest, Map.class);

                assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                return (String) loginResponse.getBody().get("token");
        }

        private boolean validateToken(String token) {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> request = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                                baseUrl + "/auth/validate", request, Map.class);

                return response.getStatusCode() == HttpStatus.OK &&
                                Boolean.TRUE.equals(response.getBody().get("valid"));
        }
}