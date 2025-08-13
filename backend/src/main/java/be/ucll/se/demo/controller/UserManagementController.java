package be.ucll.se.demo.controller;

import be.ucll.se.demo.dto.UserDTO;
import be.ucll.se.demo.dto.UserRoleUpdateDTO;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.service.UserService;
import be.ucll.se.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin(origins = "*")
public class UserManagementController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserManagementController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Check if user is admin (helper method)
    private boolean isAdminUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        String email = jwtUtil.getUsernameFromToken(token);
        return userService.isAdmin(email);
    }

    // Get all users (Admin only)
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (!isAdminUser(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID (Admin only)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        if (!isAdminUser(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        Optional<UserDTO> user = userService.getUserById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update user roles (Admin only)
    @PutMapping("/{userId}/roles")
    public ResponseEntity<?> updateUserRoles(@RequestHeader("Authorization") String authHeader,
            @PathVariable String userId,
            @Valid @RequestBody UserRoleUpdateDTO roleUpdateDTO) {
        if (!isAdminUser(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        try {
            UserDTO updatedUser = userService.updateUserRoles(userId, roleUpdateDTO);
            return ResponseEntity.ok(Map.of(
                    "message", "User roles updated successfully",
                    "user", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Enable/Disable user (Admin only)
    @PutMapping("/{userId}/toggle-enabled")
    public ResponseEntity<?> toggleUserEnabled(@RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        if (!isAdminUser(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        try {
            UserDTO updatedUser = userService.toggleUserEnabled(userId);
            String action = updatedUser.isEnabled() ? "enabled" : "disabled";
            return ResponseEntity.ok(Map.of(
                    "message", "User " + action + " successfully",
                    "user", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get users by role (Admin only)
    @GetMapping("/by-role/{role}")
    public ResponseEntity<?> getUsersByRole(@RequestHeader("Authorization") String authHeader,
            @PathVariable String role) {
        if (!isAdminUser(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        try {
            RoleName roleName = RoleName.valueOf(role.toUpperCase());
            List<UserDTO> users = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role name: " + role));
        }
    }

    // Get available roles
    @GetMapping("/available-roles")
    public ResponseEntity<?> getAvailableRoles(@RequestHeader("Authorization") String authHeader) {
        if (!isAdminUser(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        return ResponseEntity.ok(Map.of("roles", RoleName.values()));
    }
}