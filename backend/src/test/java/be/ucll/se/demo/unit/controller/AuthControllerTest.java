package be.ucll.se.demo.unit.controller;

import be.ucll.se.demo.controller.AuthController;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.service.UserService;
import be.ucll.se.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Setup test user
        testUser = new User();
        // Assuming User has setters - adjust based on your User class
        setUserField(testUser, "username", "testuser");
        setUserField(testUser, "email", "test@example.com");
        setUserField(testUser, "password", "hashedpassword");
    }

    // Helper method to set user fields (adjust based on your User class structure)
    private void setUserField(User user, String fieldName, String value) {
        try {
            switch (fieldName) {
                case "username":
                    if (hasMethod(user, "setUsername")) {
                        user.getClass().getMethod("setUsername", String.class).invoke(user, value);
                    }
                    break;
                case "email":
                    if (hasMethod(user, "setEmail")) {
                        user.getClass().getMethod("setEmail", String.class).invoke(user, value);
                    }
                    break;
                case "password":
                    if (hasMethod(user, "setPassword")) {
                        user.getClass().getMethod("setPassword", String.class).invoke(user, value);
                    }
                    break;
            }
        } catch (Exception e) {
            // If setters don't exist, we'll need to adjust the test setup
            System.out.println("Could not set field " + fieldName + " on User object");
        }
    }

    private boolean hasMethod(Object obj, String methodName) {
        try {
            obj.getClass().getMethod(methodName, String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Test
    void register_ShouldReturnSuccess_WhenValidData() throws Exception {
        // Given
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "newuser");
        registerRequest.put("email", "newuser@example.com");
        registerRequest.put("password", "password123");

        when(userService.register("newuser", "newuser@example.com", "password123"))
                .thenReturn(true);

        String jsonContent = objectMapper.writeValueAsString(registerRequest);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Geregistreerd")));

        verify(userService).register("newuser", "newuser@example.com", "password123");
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUserAlreadyExists() throws Exception {
        // Given
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "existinguser");
        registerRequest.put("email", "existing@example.com");
        registerRequest.put("password", "password123");

        when(userService.register("existinguser", "existing@example.com", "password123"))
                .thenReturn(false);

        String jsonContent = objectMapper.writeValueAsString(registerRequest);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Gebruiker of email bestaat al")));

        verify(userService).register("existinguser", "existing@example.com", "password123");
    }

    @Test
    void register_ShouldHandleMissingFields() throws Exception {
        // Given - Request with missing email
        Map<String, String> incompleteRequest = new HashMap<>();
        incompleteRequest.put("username", "user");
        incompleteRequest.put("password", "password123");
        // email is missing

        when(userService.register("user", null, "password123"))
                .thenReturn(false);

        String jsonContent = objectMapper.writeValueAsString(incompleteRequest);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Gebruiker of email bestaat al")));

        verify(userService).register("user", null, "password123");
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        when(userService.login("testuser", "password123")).thenReturn(testUser);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mocked-jwt-token");

        String jsonContent = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("mocked-jwt-token")));

        verify(userService).login("testuser", "password123");
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "wronguser");
        loginRequest.put("password", "wrongpassword");

        when(userService.login("wronguser", "wrongpassword")).thenReturn(null);

        String jsonContent = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ongeldige login")));

        verify(userService).login("wronguser", "wrongpassword");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_ShouldHandleMissingCredentials() throws Exception {
        // Given - Request with missing password
        Map<String, String> incompleteRequest = new HashMap<>();
        incompleteRequest.put("username", "testuser");
        // password is missing

        when(userService.login("testuser", null)).thenReturn(null);

        String jsonContent = objectMapper.writeValueAsString(incompleteRequest);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Ongeldige login")));

        verify(userService).login("testuser", null);
    }

    @Test
    void validateToken_ShouldReturnValid_WhenTokenIsValid() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("test@example.com");

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws Exception {
        // Given
        String invalidToken = "invalid-jwt-token";
        String bearerToken = "Bearer " + invalidToken;

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", bearerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid", is(false)));

        verify(jwtUtil).validateToken(invalidToken);
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
    }

    @Test
    void validateToken_ShouldReturnBadRequest_WhenNoAuthorizationHeader() throws Exception {
        // When & Then
        // Spring returns 400 Bad Request when a required @RequestHeader is missing
        mockMvc.perform(post("/auth/validate"))
                .andExpect(status().isBadRequest());

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenAuthorizationHeaderInvalid() throws Exception {
        // Given - Authorization header without "Bearer " prefix
        String invalidHeader = "Invalid-Token-Format";

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", invalidHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid", is(false)));

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenEmailCannotBeExtracted() throws Exception {
        // Given
        String validToken = "valid-but-no-email-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", bearerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid", is(false)));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenBearerTokenIsEmpty() throws Exception {
        // Given - Authorization header with just "Bearer "
        String emptyBearerToken = "Bearer ";

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", emptyBearerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid", is(false)));
    }

    // Edge case tests
    @Test
    void register_ShouldHandleEmptyRequestBody() throws Exception {
        // Given - Empty JSON object
        String emptyJson = "{}";

        when(userService.register(null, null, null)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Gebruiker of email bestaat al")));
    }

    @Test
    void login_ShouldHandleEmptyRequestBody() throws Exception {
        // Given - Empty JSON object
        String emptyJson = "{}";

        when(userService.login(null, null)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Ongeldige login")));
    }

    @Test
    void register_ShouldHandleSpecialCharactersInInput() throws Exception {
        // Given
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "user@#$%");
        registerRequest.put("email", "special+chars@example.com");
        registerRequest.put("password", "pass!@#$%^&*()");

        when(userService.register("user@#$%", "special+chars@example.com", "pass!@#$%^&*()"))
                .thenReturn(true);

        String jsonContent = objectMapper.writeValueAsString(registerRequest);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Geregistreerd")));

        verify(userService).register("user@#$%", "special+chars@example.com", "pass!@#$%^&*()");
    }
}