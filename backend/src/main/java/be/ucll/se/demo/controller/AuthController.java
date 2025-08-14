package be.ucll.se.demo.controller;

import be.ucll.se.demo.dto.*;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.service.UserService;
import be.ucll.se.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS,
        RequestMethod.PUT })
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Helper method for password hashing
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    // EXISTING: Basic registration (backwards compatible)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        // Default to RENTER role for backwards compatibility
        RoleName role = RoleName.RENTER;
        if (body.containsKey("role")) {
            try {
                role = RoleName.valueOf(body.get("role").toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid role. Only OWNER and RENTER are allowed."));
            }
        }

        System.out.println("Register attempt - username: " + username + ", email: " + email + ", role: " + role);

        try {
            boolean success = userService.registerWithRole(username, email, password, role);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Successfully registered",
                        "role", role.toString()));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username or email already exists"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Enhanced registration with DTO validation
    @PostMapping("/register/enhanced")
    public ResponseEntity<?> registerEnhanced(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        try {
            boolean success = userService.registerWithRole(
                    registerDTO.getUsername(),
                    registerDTO.getEmail(),
                    registerDTO.getPassword(),
                    registerDTO.getRole());

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Successfully registered",
                        "username", registerDTO.getUsername(),
                        "email", registerDTO.getEmail(),
                        "role", registerDTO.getRole().toString()));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username or email already exists"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UPDATED: Enhanced login with proper password verification
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Login attempt for user: " + username);
        System.out.println("Password received: " + password);

        // Hash the input password to compare with stored hash
        String hashedInputPassword = hashPassword(password);
        System.out.println("Hashed input password: " + hashedInputPassword);

        // Get user from database
        User user = userService.findByEmailOrUsername(username);
        if (user != null) {
            System.out.println("User found: " + user.getEmail());
            System.out.println("User enabled: " + user.isEnabled());
            System.out.println("Stored password hash: " + user.getPassword());
            System.out.println("Passwords match: " + user.getPassword().equals(hashedInputPassword));

            // Check if user is enabled and password matches
            if (user.isEnabled() && user.getPassword().equals(hashedInputPassword)) {
                // Create login response
                LoginResponseDTO loginResponse = new LoginResponseDTO();
                loginResponse.setUserId(user.getUserId());
                loginResponse.setUsername(user.getUsername());
                loginResponse.setEmail(user.getEmail());
                loginResponse.setRoles(userService.getUserRoles(user.getEmail()));

                String token = jwtUtil.generateToken(user.getEmail());
                loginResponse.setToken(token);

                System.out.println("✅ Login successful for: " + user.getEmail());
                return ResponseEntity.ok(loginResponse);
            }
        } else {
            System.out.println("❌ User not found: " + username);
        }

        System.out.println("❌ Login failed for user: " + username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials or account disabled"));
    }

    // UPDATED: Enhanced token validation with role information
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }

        String email = jwtUtil.getUsernameFromToken(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }

        // Get user roles
        Set<RoleName> userRoles = userService.getUserRoles(email);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("user", Map.of(
                "email", email,
                "roles", userRoles,
                "isAdmin", userRoles.contains(RoleName.ADMIN)));

        return ResponseEntity.ok(response);
    }

    // NEW: Check if user has specific role
    @GetMapping("/check-role/{role}")
    public ResponseEntity<?> checkUserRole(@RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String role) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("hasRole", false));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("hasRole", false));
        }

        String email = jwtUtil.getUsernameFromToken(token);
        try {
            RoleName roleName = RoleName.valueOf(role.toUpperCase());
            boolean hasRole = userService.userHasRole(email, roleName);
            return ResponseEntity.ok(Map.of("hasRole", hasRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role name"));
        }
    }
}