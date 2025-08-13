// DataInitializer.java
package be.ucll.se.demo.init;

import be.ucll.se.demo.model.Role;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.repository.RoleRepository;
import be.ucll.se.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        initializeRoles();

        // Create default admin user if no admin exists
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
        // Check if any admin user exists
        boolean adminExists = userRepository.findByRole(RoleName.ADMIN)
                .stream()
                .anyMatch(User::isEnabled);

        if (!adminExists) {
            // Create default admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@car4rent.com");
            admin.setPassword(Base64.getEncoder().encodeToString("admin123".getBytes()));
            admin.setEnabled(true);

            // Add admin role
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            admin.addRole(adminRole);

            userRepository.save(admin);
            System.out.println("✅ Created default admin user:");
            System.out.println("   Username: admin");
            System.out.println("   Email: admin@car4rent.com");
            System.out.println("   Password: admin123");
        }
    }
}