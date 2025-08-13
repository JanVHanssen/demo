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

    // UPDATED: Enhanced login with role information
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        System.out.println("Login attempt for user: " + username);

        LoginResponseDTO loginResponse = userService.loginWithRoles(username, password);

        if (loginResponse != null) {
            System.out.println("User found: " + loginResponse.getUsername() +
                    ", email: " + loginResponse.getEmail() +
                    ", roles: " + loginResponse.getRoles());

            String token = jwtUtil.generateToken(loginResponse.getEmail());
            loginResponse.setToken(token);

            System.out.println("Generated token with email: " + loginResponse.getEmail());
            return ResponseEntity.ok(loginResponse);
        } else {
            System.out.println("Login failed for user: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials or account disabled"));
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