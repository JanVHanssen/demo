package be.ucll.se.demo.unit.controller;

import be.ucll.se.demo.controller.AuthController;
import be.ucll.se.demo.dto.LoginResponseDTO;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.service.UserService;
import be.ucll.se.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private User testUser;
    private LoginResponseDTO testLoginResponse;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        // Setup test user with SHA-256 hashed password
        testUser = new User();
        setUserField(testUser, "username", "testuser");
        setUserField(testUser, "email", "test@example.com");
        setUserField(testUser, "password", hashPassword("hashedpassword"));

        // Setup test login response
        testLoginResponse = new LoginResponseDTO();
        testLoginResponse.setUserId("test-uuid");
        testLoginResponse.setUsername("testuser");
        testLoginResponse.setEmail("test@example.com");
        testLoginResponse.setRoles(Set.of(RoleName.RENTER));
    }

    /**
     * Helper method to hash passwords using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // Helper method to set user fields (unchanged)
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
    void register_ShouldReturnBadRequest_WhenFieldsMissing() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", ""); // leeg
        request.put("email", ""); // leeg
        request.put("password", "pass");

        String jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerWithRole(any(), any(), any(), any());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenInvalidRole() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "user1");
        body.put("email", "test@example.com");
        body.put("password", "password");
        body.put("role", "INVALID_ROLE");

        ResponseEntity<?> response = authController.register(body);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("Invalid role"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUsernameExists() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", "existing");
        request.put("email", "email@example.com");
        request.put("password", "pass");

        when(userService.registerWithRole(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Username or email already exists"));

        String jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Username or email already exists")));

        verify(userService).registerWithRole("existing", "email@example.com", "pass", RoleName.RENTER);
    }

    @Test
    void register_ShouldReturnSuccess_WithDefaultRole() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("email", "new@example.com");
        request.put("password", "pass");

        User mockUser = new User();
        mockUser.setUserId("1");
        mockUser.setUsername("newuser");
        mockUser.setEmail("new@example.com");

        when(userService.registerWithRole(any(), any(), any(), eq(RoleName.RENTER)))
                .thenReturn(mockUser);

        String jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Successfully registered")))
                .andExpect(jsonPath("$.userId", is("1")))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.role", is("RENTER")));
    }

    @Test
    void register_ShouldReturnSuccess_WithOwnerRole() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", "owneruser");
        request.put("email", "owner@example.com");
        request.put("password", "pass");
        request.put("role", "OWNER");

        User mockUser = new User();
        mockUser.setUserId("2");
        mockUser.setUsername("owneruser");
        mockUser.setEmail("owner@example.com");

        when(userService.registerWithRole(any(), any(), any(), eq(RoleName.OWNER)))
                .thenReturn(mockUser);

        String jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Successfully registered")))
                .andExpect(jsonPath("$.userId", is("2")))
                .andExpect(jsonPath("$.username", is("owneruser")))
                .andExpect(jsonPath("$.email", is("owner@example.com")))
                .andExpect(jsonPath("$.role", is("OWNER")));
    }

    @Test
    void login_ShouldReturnTokenWithRoles_WhenValidCredentials() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        when(userService.loginWithRoles("testuser", "password123")).thenReturn(testLoginResponse);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mocked-jwt-token");

        String jsonContent = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("mocked-jwt-token")))
                .andExpect(jsonPath("$.userId", is("test-uuid")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.roles", hasItem("RENTER")));

        verify(userService).loginWithRoles("testuser", "password123");
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "wronguser");
        loginRequest.put("password", "wrongpassword");

        when(userService.loginWithRoles("wronguser", "wrongpassword")).thenReturn(null);

        String jsonContent = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Invalid credentials or account disabled")));

        verify(userService).loginWithRoles("wronguser", "wrongpassword");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void validateToken_ShouldReturnValidWithRoles_WhenTokenIsValid() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userService.getUserRoles("test@example.com")).thenReturn(Set.of(RoleName.RENTER));

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.user.email", is("test@example.com")))
                .andExpect(jsonPath("$.user.roles", hasItem("RENTER")))
                .andExpect(jsonPath("$.user.isAdmin", is(false)));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(userService).getUserRoles("test@example.com");
    }

    @Test
    void validateToken_ShouldReturnValidWithAdminRole_WhenUserIsAdmin() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("admin@example.com");
        when(userService.getUserRoles("admin@example.com")).thenReturn(Set.of(RoleName.ADMIN));

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.user.email", is("admin@example.com")))
                .andExpect(jsonPath("$.user.roles", hasItem("ADMIN")))
                .andExpect(jsonPath("$.user.isAdmin", is(true)));
    }

    @Test
    void checkUserRole_ShouldReturnTrue_WhenUserHasRole() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userService.userHasRole("test@example.com", RoleName.RENTER)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/auth/check-role/renter")
                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRole", is(true)));

        verify(userService).userHasRole("test@example.com", RoleName.RENTER);
    }

    @Test
    void checkUserRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userService.userHasRole("test@example.com", RoleName.ADMIN)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/auth/check-role/admin")
                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRole", is(false)));

        verify(userService).userHasRole("test@example.com", RoleName.ADMIN);
    }

    @Test
    void checkUserRole_ShouldReturnUnauthorized_WhenNoToken() throws Exception {
        // With required = false, missing header returns 401 instead of 500
        mockMvc.perform(get("/auth/check-role/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.hasRole", is(false)));

        verify(userService, never()).userHasRole(anyString(), any(RoleName.class));
    }

    @Test
    void checkUserRole_ShouldReturnBadRequest_WhenInvalidRole() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("test@example.com");

        // When & Then
        mockMvc.perform(get("/auth/check-role/invalid-role")
                .header("Authorization", bearerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid role name")));
    }

    // Keep existing tests for backwards compatibility
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
    void validateToken_ShouldReturn500_WhenNoAuthorizationHeader() throws Exception {
        // When & Then - Spring throws exception when required @RequestHeader is missing
        mockMvc.perform(post("/auth/validate"))
                .andExpect(status().isInternalServerError());

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
    }

    @Test
    void register_ShouldHandleMissingFields() throws Exception {
        Map<String, String> incompleteRequest = new HashMap<>();
        incompleteRequest.put("username", "user");
        incompleteRequest.put("password", "password123");

        String jsonContent = objectMapper.writeValueAsString(incompleteRequest);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Username or email already exists")));

        // Controleer dat registerWithRole NIET wordt aangeroepen
        verify(userService, never()).registerWithRole(any(), any(), any(), any());
    }

}