package be.ucll.se.demo.init;

import be.ucll.se.demo.model.Role;
import be.ucll.se.demo.model.RoleName;
import be.ucll.se.demo.model.User;
import be.ucll.se.demo.model.Notification;
import be.ucll.se.demo.model.NotificationType;
import be.ucll.se.demo.model.NotificationStatus;
import be.ucll.se.demo.repository.RoleRepository;
import be.ucll.se.demo.repository.UserRepository;
import be.ucll.se.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeDefaultAdmin();
        initializeTestUsers();
        initializeTestNotifications();
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

    private void initializeTestUsers() {
        // Test Owner
        if (userRepository.findByEmail("owner@test.com").isEmpty()) {
            User owner = new User();
            owner.setUsername("testowner");
            owner.setEmail("owner@test.com");
            owner.setPassword(hashPassword("password123"));
            owner.setEnabled(true);

            Role ownerRole = roleRepository.findByName(RoleName.OWNER)
                    .orElseThrow(() -> new RuntimeException("Owner role not found"));
            owner.addRole(ownerRole);

            userRepository.save(owner);
            System.out.println("✅ Created test owner user");
        }

        // Test Renter
        if (userRepository.findByEmail("renter@test.com").isEmpty()) {
            User renter = new User();
            renter.setUsername("testrenter");
            renter.setEmail("renter@test.com");
            renter.setPassword(hashPassword("password123"));
            renter.setEnabled(true);

            Role renterRole = roleRepository.findByName(RoleName.RENTER)
                    .orElseThrow(() -> new RuntimeException("Renter role not found"));
            renter.addRole(renterRole);

            userRepository.save(renter);
            System.out.println("✅ Created test renter user");
        }

        // Test Accountant
        if (userRepository.findByEmail("accountant@test.com").isEmpty()) {
            User accountant = new User();
            accountant.setUsername("testaccountant");
            accountant.setEmail("accountant@test.com");
            accountant.setPassword(hashPassword("password123"));
            accountant.setEnabled(true);

            Role accountantRole = roleRepository.findByName(RoleName.ACCOUNTANT)
                    .orElseThrow(() -> new RuntimeException("Accountant role not found"));
            accountant.addRole(accountantRole);

            userRepository.save(accountant);
            System.out.println("✅ Created test accountant user");
        }
    }

    private void initializeTestNotifications() {
        // Only create notifications if none exist
        if (notificationRepository.count() == 0) {

            // Admin notifications
            createNotification("admin@car4rent.com", NotificationType.SYSTEM_ANNOUNCEMENT,
                    "🎉 Welkom bij Car4Rent!",
                    "Het notificatiesysteem is succesvol geïnstalleerd. Je kunt nu alle platform activiteiten volgen.",
                    null, null, NotificationStatus.SENT, LocalDateTime.now().minusHours(1));

            createNotification("admin@car4rent.com", NotificationType.SYSTEM_ANNOUNCEMENT,
                    "📊 Systeem Update",
                    "Een nieuwe versie van het platform is gedeployed met verbeterde notificaties en user management.",
                    null, null, NotificationStatus.SENT, LocalDateTime.now().minusMinutes(30));

            // Owner notifications
            createNotification("owner@test.com", NotificationType.NEW_BOOKING,
                    "🚗 Nieuwe boeking ontvangen!",
                    "Je BMW X3 is geboekt van 25/08/2025 tot 30/08/2025 door renter@test.com. Contacteer de huurder op +32 478 123 456 voor verdere afspraken.",
                    1L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusMinutes(45));

            createNotification("owner@test.com", NotificationType.NEW_BOOKING,
                    "🚗 Nieuwe boeking ontvangen!",
                    "Je Audi A4 is geboekt van 28/08/2025 tot 02/09/2025 door john.doe@email.com. Contacteer de huurder op +32 498 765 432 voor verdere afspraken.",
                    2L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusMinutes(20));

            createNotification("owner@test.com", NotificationType.BOOKING_CANCELLED,
                    "❌ Boeking geannuleerd",
                    "De boeking van je Mercedes C-Class voor 22/08/2025 tot 25/08/2025 is geannuleerd door de huurder.",
                    3L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusHours(2));

            // Renter notifications
            createNotification("renter@test.com", NotificationType.BOOKING_CONFIRMATION,
                    "✅ Boeking bevestigd!",
                    "Je boeking voor BMW X3 van 25/08/2025 tot 30/08/2025 is bevestigd. Vergeet niet je rijbewijs en identiteitskaart mee te nemen bij het ophalen.",
                    1L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusMinutes(40));

            createNotification("renter@test.com", NotificationType.RENTAL_REMINDER,
                    "⏰ Herinnering: Auto ophalen morgen",
                    "Vergeet niet om morgen je gehuurde BMW X3 op te halen om 10:00 in Leuven Centrum. Neem je rijbewijs en identiteitskaart mee.",
                    1L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusMinutes(15));

            createNotification("renter@test.com", NotificationType.RETURN_REMINDER,
                    "🔄 Herinnering: Auto terugbrengen",
                    "Je gehuurde Volkswagen Golf moet morgen om 18:00 teruggebracht worden. Zorg ervoor dat je op tijd bent!",
                    4L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusMinutes(5));

            // Accountant notifications
            createNotification("accountant@test.com", NotificationType.SYSTEM_ANNOUNCEMENT,
                    "📈 Maandelijkse Rapportage",
                    "De rapportage voor augustus 2025 is beschikbaar. Er zijn 47 nieuwe boekingen geregistreerd met een totale omzet van €12,450.",
                    null, null, NotificationStatus.SENT, LocalDateTime.now().minusHours(3));

            // Recent unread notifications
            createNotification("admin@car4rent.com", NotificationType.SYSTEM_ANNOUNCEMENT,
                    "🔧 Maintenance Melding",
                    "Het systeem zal vanavond om 23:00 kort offline zijn voor onderhoud. Verwachte downtime: 15 minuten.",
                    null, null, NotificationStatus.SENT, LocalDateTime.now().minusMinutes(2));

            createNotification("owner@test.com", NotificationType.NEW_BOOKING,
                    "🚗 Nieuwe boeking ontvangen!",
                    "Je Tesla Model 3 is zojuist geboekt voor dit weekend door sarah.johnson@email.com. Contacteer de huurder voor ophaaldetails.",
                    5L, "RENT", NotificationStatus.SENT, LocalDateTime.now().minusMinutes(1));

            System.out.println("✅ Created test notifications for all user types");
        }
    }

    private void createNotification(String recipientEmail, NotificationType type, String title,
            String message, Long relatedEntityId, String relatedEntityType,
            NotificationStatus status, LocalDateTime createdAt) {
        Notification notification = new Notification();
        notification.setRecipientEmail(recipientEmail);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setStatus(status);
        notification.setCreatedAt(createdAt);

        if (status == NotificationStatus.SENT) {
            notification.setSentAt(createdAt.plusSeconds(1));
        }

        notificationRepository.save(notification);
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