package be.ucll.se.demo.init;

import be.ucll.se.demo.model.Role;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.repository.RoleRepository;
import be.ucll.se.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeDefaultAdmin();
    }

    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role(roleName);
                roleRepository.save(role);
                System.out.println("✅ Created role: " + roleName);
            }
        }
    }

    private void initializeDefaultAdmin() {
        List<User> admins = userRepository.findByRole(RoleName.ADMIN);
        boolean adminExists = admins.stream().anyMatch(User::isEnabled);

        if (!adminExists) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@car4rent.com");
            admin.setPassword(hashPassword("admin123")); // SHA-256 hash
            admin.setEnabled(true);

            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            admin.addRole(adminRole);

            userRepository.save(admin);
            System.out.println("✅ Created default admin user");
        }
    }

    // Exact dezelfde SHA-256 hashfunctie als in UserService
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
