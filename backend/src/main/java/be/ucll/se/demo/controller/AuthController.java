
package be.ucll.se.demo.controller;

import be.ucll.se.demo.model.User;
import be.ucll.se.demo.service.UserService;
import be.ucll.se.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS })
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email"); //
        String password = body.get("password");

        System.out.println("Register attempt - username: " + username + ", email: " + email);

        boolean success = userService.register(username, email, password);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Geregistreerd"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Gebruiker of email bestaat al"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        System.out.println("Login attempt for user: " + username);

        User user = userService.login(username, password);

        if (user != null) {
            System.out.println("User found: " + user.getUsername() + ", email: " + user.getEmail());

            String token = jwtUtil.generateToken(user.getEmail());
            System.out.println("Generated token with email: " + user.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            System.out.println("Login failed for user: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Ongeldige login"));
        }
    }

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

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("user", Map.of("email", email));

        return ResponseEntity.ok(response);
    }
}