package be.ucll.se.demo.unit.service;

import be.ucll.se.demo.dto.LoginResponseDTO;
import be.ucll.se.demo.dto.UserDTO;
import be.ucll.se.demo.dto.UserRoleUpdateDTO;
import be.ucll.se.demo.model.Role;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.repository.RoleRepository;
import be.ucll.se.demo.repository.UserRepository;
import be.ucll.se.demo.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role renterRole;
    private Role ownerRole;
    private Role adminRole;
    private String testPassword = "testPassword123";
    private String testHashedPassword;

    @BeforeEach
    void setUp() {
        testHashedPassword = hashPassword(testPassword);

        // Setup roles
        renterRole = new Role(RoleName.RENTER);
        renterRole.setId(1L);

        ownerRole = new Role(RoleName.OWNER);
        ownerRole.setId(2L);

        adminRole = new Role(RoleName.ADMIN);
        adminRole.setId(3L);

        testUser = createTestUser();

        // Setup role repository mocks with lenient() to avoid unnecessary stubbing
        // warnings
        lenient().when(roleRepository.findByName(RoleName.RENTER)).thenReturn(Optional.of(renterRole));
        lenient().when(roleRepository.findByName(RoleName.OWNER)).thenReturn(Optional.of(ownerRole));
        lenient().when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
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

    // ===== REGISTER TESTS (Updated for new role-based registration) =====
    @Test
    void register_WhenNewUser_ShouldCreateUserWithDefaultRenterRole() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When - using default register method (should default to RENTER)
        boolean result = userService.register(username, email, password);

        // Then
        assertThat(result).isTrue();

        // Verify user was saved with correct data and RENTER role
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(username);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(hashPassword(password)); // Updated to use SHA-256
        assertThat(savedUser.getRoles()).contains(renterRole);

        verify(userRepository).findByUsername(username);
        verify(userRepository).findByEmail(email);
        verify(roleRepository).findByName(RoleName.RENTER);
    }

    @Test
    void registerWithRole_WhenNewUserWithOwnerRole_ShouldCreateUserWithOwnerRole() {
        // Given
        String username = "newowner";
        String email = "owner@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.registerWithRole(username, email, password, RoleName.OWNER);

        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(username);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(hashPassword(password)); // Updated to use SHA-256
        assertThat(savedUser.getRoles()).contains(ownerRole);

        verify(roleRepository).findByName(RoleName.OWNER);
    }

    @Test
    void registerWithRole_WhenInvalidRole_ShouldThrowException() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> userService.registerWithRole(username, email, password, RoleName.ADMIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Invalid role selection. Only OWNER and RENTER roles are allowed during registration.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenUsernameAlreadyExists_ShouldReturnFalse() {
        // Given
        String username = "existinguser";
        String email = "newemail@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.register(username, email, password);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturnFalse() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.register(username, email, password);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByUsername(username);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    // ===== LOGIN TESTS (Updated for new enhanced login) =====
    @Test
    void login_WhenValidCredentials_ShouldReturnUser() {
        // Given
        String username = "testuser";
        String password = testPassword;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.login(username, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void login_WhenUserDisabled_ShouldReturnNull() {
        // Given
        String username = "testuser";
        String password = testPassword;
        testUser.setEnabled(false); // Disable user

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.login(username, password);

        // Then
        assertThat(result).isNull();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loginWithRoles_WhenValidCredentials_ShouldReturnLoginResponseWithRoles() {
        // Given
        String username = "testuser";
        String password = testPassword;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        LoginResponseDTO result = userService.loginWithRoles(username, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getRoles()).contains(RoleName.RENTER);
    }

    @Test
    void login_WhenUserNotFound_ShouldReturnNull() {
        // Given
        String username = "nonexistent";
        String password = "anypassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        User result = userService.login(username, password);

        // Then
        assertThat(result).isNull();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void login_WhenWrongPassword_ShouldReturnNull() {
        // Given
        String username = "testuser";
        String wrongPassword = "wrongpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.login(username, wrongPassword);

        // Then
        assertThat(result).isNull();
        verify(userRepository).findByUsername(username);
    }

    // ===== ROLE MANAGEMENT TESTS =====
    @Test
    void updateUserRoles_WhenValidUser_ShouldUpdateRoles() {
        // Given
        String userId = "test-user-id";
        UserRoleUpdateDTO roleUpdateDTO = new UserRoleUpdateDTO();
        roleUpdateDTO.setRoles(Set.of(RoleName.OWNER, RoleName.RENTER));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.updateUserRoles(userId, roleUpdateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
        verify(roleRepository).findByName(RoleName.OWNER);
        verify(roleRepository).findByName(RoleName.RENTER);
    }

    @Test
    void updateUserRoles_WhenUserNotFound_ShouldThrowException() {
        // Given
        String userId = "nonexistent";
        UserRoleUpdateDTO roleUpdateDTO = new UserRoleUpdateDTO();
        roleUpdateDTO.setRoles(Set.of(RoleName.RENTER));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserRoles(userId, roleUpdateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: " + userId);

        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserEnabled_WhenValidUser_ShouldToggleEnabledStatus() {
        // Given
        String userId = "test-user-id";
        boolean originalStatus = testUser.isEnabled();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.toggleUserEnabled(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.isEnabled()).isEqualTo(!originalStatus);
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void getUsersByRole_WhenUsersExist_ShouldReturnUsers() {
        // Given
        List<User> users = List.of(testUser);
        when(userRepository.findByRole(RoleName.RENTER)).thenReturn(users);

        // When
        List<UserDTO> result = userService.getUsersByRole(RoleName.RENTER);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(testUser.getUsername());
        verify(userRepository).findByRole(RoleName.RENTER);
    }

    @Test
    void userHasRole_WhenUserHasRole_ShouldReturnTrue() {
        // Given
        String email = "test@example.com";
        when(userRepository.userHasRole(email, RoleName.RENTER)).thenReturn(true);

        // When
        boolean result = userService.userHasRole(email, RoleName.RENTER);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).userHasRole(email, RoleName.RENTER);
    }

    @Test
    void isAdmin_WhenUserIsAdmin_ShouldReturnTrue() {
        // Given
        String email = "admin@example.com";
        when(userRepository.userHasRole(email, RoleName.ADMIN)).thenReturn(true);

        // When
        boolean result = userService.isAdmin(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).userHasRole(email, RoleName.ADMIN);
    }

    @Test
    void getUserRoles_WhenUserExists_ShouldReturnRoles() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Set<RoleName> result = userService.getUserRoles(email);

        // Then
        assertThat(result).contains(RoleName.RENTER);
        verify(userRepository).findByEmail(email);
    }

    // ===== FIND BY EMAIL TESTS =====
    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_WhenUserNotFound_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(email);
    }

    // ===== INTEGRATION-STYLE TESTS =====
    @Test
    void registerAndLogin_ShouldWorkTogether() {
        // Given
        String username = "integrationuser";
        String email = "integration@example.com";
        String password = "integrationPassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When - register
        boolean registerResult = userService.register(username, email, password);

        // Capture the saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Mock the repository to return the saved user for login
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(savedUser));

        // When - login
        User loginResult = userService.login(username, password);

        // Then
        assertThat(registerResult).isTrue();
        assertThat(loginResult).isNotNull();
        assertThat(loginResult.getUsername()).isEqualTo(username);
        assertThat(loginResult.getEmail()).isEqualTo(email);
        // Verify the password was properly hashed during registration
        assertThat(savedUser.getPassword()).isEqualTo(hashPassword(password));
    }

    // ===== PASSWORD HASHING SPECIFIC TESTS =====
    @Test
    void register_ShouldHashPasswordWithSHA256() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String plainPassword = "myPlainPassword123";
        String expectedHashedPassword = hashPassword(plainPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        userService.register(username, email, plainPassword);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(expectedHashedPassword);
        assertThat(savedUser.getPassword()).isNotEqualTo(plainPassword); // Ensure it's not stored as plain text
        assertThat(savedUser.getPassword()).hasSize(64); // SHA-256 produces a 64-character hex string
    }

    @Test
    void login_ShouldVerifyPasswordAgainstSHA256Hash() {
        // Given
        String username = "testuser";
        String plainPassword = testPassword;
        // testUser already has SHA-256 hashed password from setUp()

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.login(username, plainPassword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser);
        // The service should have successfully verified the plain password against the
        // stored hash
    }

    @Test
    void login_ShouldFailWithWrongPasswordAgainstSHA256Hash() {
        // Given
        String username = "testuser";
        String wrongPassword = "wrongPassword123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.login(username, wrongPassword);

        // Then
        assertThat(result).isNull(); // Login should fail with wrong password
    }

    // ===== HELPER METHODS =====
    private User createTestUser() {
        User user = new User();
        user.setUserId("test-user-id");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(testHashedPassword); // Now using SHA-256 hashed password
        user.setEnabled(true);

        // Add default RENTER role
        user.setRoles(new HashSet<>());
        user.addRole(renterRole);

        return user;
    }
}