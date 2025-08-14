package be.ucll.se.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.ucll.se.demo.dto.*;
import be.ucll.se.demo.model.Role;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.repository.RoleRepository;
import be.ucll.se.demo.repository.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public boolean register(String username, String email, String password) {
        try {
            registerWithRole(username, email, password, RoleName.RENTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Alternative fix: gebruik persist instead of save
    @Transactional
    public User registerWithRole(String username, String email, String password, RoleName roleName) {
        boolean usernameTaken = userRepository.findByUsername(username).isPresent();
        boolean emailTaken = userRepository.findByEmail(email).isPresent();

        if (usernameTaken || emailTaken) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        if (roleName != RoleName.OWNER && roleName != RoleName.RENTER) {
            throw new IllegalArgumentException(
                    "Invalid role selection. Only OWNER and RENTER roles are allowed during registration.");
        }

        // Haal de role op VOOR je de user maakt
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        String hashedPassword = hashPassword(password);
        User user = new User();
        // NIET user.setUserId() - laat Hibernate de ID genereren
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setEnabled(true);

        // Initialize roles set
        user.setRoles(new HashSet<>());
        user.addRole(role); // Voeg de role toe VOOR het saven

        return userRepository.save(user);
    }

    // EXISTING: Login method
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isEnabled() && verifyPassword(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    // EXISTING: Find by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // NEW: Get all users (Admin only)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // NEW: Get user by ID with DTO
    public Optional<UserDTO> getUserById(String userId) {
        return userRepository.findById(userId)
                .map(this::convertToDTO);
    }

    // NEW: Update user roles (Admin only)
    public UserDTO updateUserRoles(String userId, UserRoleUpdateDTO roleUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Clear existing roles
        user.getRoles().clear();

        // Add new roles
        for (RoleName roleName : roleUpdateDTO.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            user.addRole(role);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // NEW: Enable/Disable user (Admin only)
    public UserDTO toggleUserEnabled(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setEnabled(!user.isEnabled());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // NEW: Get users by role
    public List<UserDTO> getUsersByRole(RoleName roleName) {
        return userRepository.findByRole(roleName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // NEW: Check if user has specific role
    public boolean userHasRole(String email, RoleName roleName) {
        return userRepository.userHasRole(email, roleName);
    }

    // NEW: Check if current user is admin
    public boolean isAdmin(String email) {
        return userHasRole(email, RoleName.ADMIN);
    }

    // NEW: Get user's roles
    public Set<RoleName> getUserRoles(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    // FIXED: Hash password using SHA-256 with hexadecimal output
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

    // FIXED: Verify password against SHA-256 hash
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        String inputHash = hashPassword(plainPassword);
        return inputHash.equals(hashedPassword);
    }

    // NEW: Convert User to DTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());

        Set<RoleName> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);

        return dto;
    }

    // NEW: Enhanced login response with roles
    public LoginResponseDTO loginWithRoles(String username, String password) {
        User user = login(username, password);
        if (user != null) {
            LoginResponseDTO response = new LoginResponseDTO();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());

            Set<RoleName> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            response.setRoles(roles);

            return response;
        }
        return null;
    }

    public User findByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByEmail(emailOrUsername)
                .orElse(userRepository.findByUsername(emailOrUsername).orElse(null));
    }
}