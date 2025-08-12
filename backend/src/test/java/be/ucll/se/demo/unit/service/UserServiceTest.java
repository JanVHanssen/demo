package be.ucll.se.demo.unit.service;

import be.ucll.se.demo.model.User;
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
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String testPassword = "testPassword123";
    private String testHashedPassword;

    @BeforeEach
    void setUp() {
        testHashedPassword = Base64.getEncoder().encodeToString(testPassword.getBytes());
        testUser = createTestUser();
    }

    // ===== REGISTER TESTS =====
    @Test
    void register_WhenNewUser_ShouldCreateUserAndReturnTrue() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.register(username, email, password);

        // Then
        assertThat(result).isTrue();

        // Verify user was saved with correct data
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(username);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(Base64.getEncoder().encodeToString(password.getBytes()));

        verify(userRepository).findByUsername(username);
        verify(userRepository).findByEmail(email);
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

    @Test
    void register_WhenBothUsernameAndEmailExist_ShouldReturnFalseAfterUsernameCheck() {
        // Given
        String username = "existinguser";
        String email = "existing@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.register(username, email, password);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).findByEmail(anyString()); // Should not check email if username exists
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldHashPasswordCorrectly() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "mySecretPassword";
        String expectedHash = Base64.getEncoder().encodeToString(password.getBytes());

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        userService.register(username, email, password);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(expectedHash);
    }

    // ===== LOGIN TESTS =====
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

    @Test
    void login_WhenEmptyPassword_ShouldReturnNull() {
        // Given
        String username = "testuser";
        String emptyPassword = "";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.login(username, emptyPassword);

        // Then
        assertThat(result).isNull();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void login_WhenNullPassword_ShouldThrowNullPointerException() {
        // Given
        String username = "testuser";
        String nullPassword = null;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.login(username, nullPassword))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Cannot invoke \"String.getBytes()\" because \"password\" is null");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void login_ShouldVerifyPasswordCorrectly() {
        // Given
        String username = "testuser";
        String correctPassword = testPassword;
        String wrongPassword = "wrongPassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When - correct password
        User successResult = userService.login(username, correctPassword);

        // When - wrong password
        User failResult = userService.login(username, wrongPassword);

        // Then
        assertThat(successResult).isNotNull();
        assertThat(failResult).isNull();
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

    @Test
    void findByEmail_WhenNullEmail_ShouldDelegateToRepository() {
        // Given
        String nullEmail = null;
        when(userRepository.findByEmail(nullEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(nullEmail);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(nullEmail);
    }

    @Test
    void findByEmail_WhenEmptyEmail_ShouldDelegateToRepository() {
        // Given
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(emptyEmail);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(emptyEmail);
    }

    // ===== PASSWORD HASHING TESTS (via register) =====
    @Test
    void register_ShouldHandleSpecialCharactersInPassword() {
        // Given
        String username = "specialuser";
        String email = "special@example.com";
        String passwordWithSpecialChars = "p@ssw0rd!#$%^&*()";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.register(username, email, passwordWithSpecialChars);

        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isNotEqualTo(passwordWithSpecialChars); // Should be hashed
        assertThat(savedUser.getPassword())
                .isEqualTo(Base64.getEncoder().encodeToString(passwordWithSpecialChars.getBytes()));
    }

    @Test
    void register_ShouldHandleUnicodeCharactersInPassword() {
        // Given
        String username = "unicodeuser";
        String email = "unicode@example.com";
        String unicodePassword = "pássw💙rd";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.register(username, email, unicodePassword);

        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(Base64.getEncoder().encodeToString(unicodePassword.getBytes()));
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
    }

    // ===== HELPER METHODS =====
    private User createTestUser() {
        User user = new User();
        user.setUserId("test-user-id");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(testHashedPassword);
        return user;
    }
}