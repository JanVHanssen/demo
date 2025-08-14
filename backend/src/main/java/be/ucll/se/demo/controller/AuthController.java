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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    // FIXED: Helper method for SHA-256 password hashing (hexadecimal output)
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        // Check voor verplichte velden
        if (username == null || username.isBlank() || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username or email already exists"));
        }

        // Default to RENTER role
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
            User user = userService.registerWithRole(username, email, password, role);

            return ResponseEntity.ok(Map.of(
                    "message", "Successfully registered",
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", role.toString()));

        } catch (IllegalArgumentException e) {
            // Hier wordt duplicate username/email afgehandeld
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    // NEW: Enhanced registration with DTO validation
    @PostMapping("/register/enhanced")
    public ResponseEntity<?> registerEnhanced(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        try {
            // Verander van boolean naar User
            User user = userService.registerWithRole(
                    registerDTO.getUsername(),
                    registerDTO.getEmail(),
                    registerDTO.getPassword(),
                    registerDTO.getRole());

            return ResponseEntity.ok(Map.of(
                    "message", "Successfully registered",
                    "userId", user.getUserId(),
                    "username", registerDTO.getUsername(),
                    "email", registerDTO.getEmail(),
                    "role", registerDTO.getRole().toString()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Enhanced registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    // FIXED: Updated login to use loginWithRoles and proper response format
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Login attempt for user: " + username);
        System.out.println("Password received: " + password);

        try {
            // Use loginWithRoles as expected by tests
            LoginResponseDTO loginResponse = userService.loginWithRoles(username, password);

            if (loginResponse != null) {
                System.out.println("✓ Login successful for user: " + username);
                String token = jwtUtil.generateToken(loginResponse.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("userId", loginResponse.getUserId());
                response.put("username", loginResponse.getUsername());
                response.put("email", loginResponse.getEmail());
                response.put("roles", loginResponse.getRoles());

                return ResponseEntity.ok(response);
            } else {
                System.out.println("✗ Login failed for user: " + username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid credentials or account disabled");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            System.out.println("✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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