package be.ucll.se.demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ucll.se.demo.dto.NotificationDTO;
import be.ucll.se.demo.model.Notification;
import be.ucll.se.demo.model.NotificationStatus;
import be.ucll.se.demo.model.NotificationType;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.repository.NotificationRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService; // Nieuwe service voor email verzending

    // === CORE NOTIFICATION METHODS ===

    public Notification createNotification(String recipientEmail, NotificationType type,
            String title, String message,
            Long relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification(recipientEmail, type, title, message,
                relatedEntityId, relatedEntityType);
        return notificationRepository.save(notification);
    }

    public void sendNotification(Notification notification) {
        try {
            // Verstuur email
            emailService.sendNotificationEmail(notification);

            // Update status
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            // Log error
        }
    }

    public Notification createAndSendNotification(String recipientEmail, NotificationType type,
            String title, String message,
            Long relatedEntityId, String relatedEntityType) {
        Notification notification = createNotification(recipientEmail, type, title, message,
                relatedEntityId, relatedEntityType);
        sendNotification(notification);
        return notification;
    }

    // === BUSINESS-SPECIFIC NOTIFICATIONS ===

    public void notifyOwnerOfNewBooking(Rent rent) {
        String title = "Nieuwe autoboeking ontvangen!";
        String message = String.format(
                "Je auto is geboekt van %s tot %s door %s. " +
                        "Contacteer de huurder op %s voor verdere afspraken.",
                rent.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                rent.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                rent.getRenterEmail(),
                rent.getRenterInfo().getPhoneNumber());

        createAndSendNotification(rent.getOwnerEmail(), NotificationType.NEW_BOOKING,
                title, message, rent.getId(), "RENT");
    }

    public void notifyRenterOfConfirmation(Rent rent) {
        String title = "Boeking bevestigd!";
        String message = String.format(
                "Je boeking voor %s %s van %s tot %s is bevestigd. " +
                        "Vergeet niet je rijbewijs en identiteitskaart mee te nemen bij het ophalen.",
                rent.getCar().getBrand(),
                rent.getCar().getModel(),
                rent.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                rent.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        createAndSendNotification(rent.getRenterEmail(), NotificationType.BOOKING_CONFIRMATION,
                title, message, rent.getId(), "RENT");
    }

    public void notifyBookingCancellation(Rent rent) {
        // Notify owner
        String ownerTitle = "Boeking geannuleerd";
        String ownerMessage = String.format(
                "De boeking van je auto %s %s voor %s tot %s is geannuleerd.",
                rent.getCar().getBrand(), rent.getCar().getModel(),
                rent.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                rent.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        createAndSendNotification(rent.getOwnerEmail(), NotificationType.BOOKING_CANCELLED,
                ownerTitle, ownerMessage, rent.getId(), "RENT");

        // Notify renter
        String renterTitle = "Je boeking is geannuleerd";
        String renterMessage = String.format(
                "Je boeking voor %s %s van %s tot %s is geannuleerd.",
                rent.getCar().getBrand(), rent.getCar().getModel(),
                rent.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                rent.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        createAndSendNotification(rent.getRenterEmail(), NotificationType.BOOKING_CANCELLED,
                renterTitle, renterMessage, rent.getId(), "RENT");
    }

    public void sendRentalReminder(Rent rent) {
        String title = "Herinnering: Auto ophalen morgen";
        String message = String.format(
                "Vergeet niet om morgen je gehuurde %s %s op te halen. " +
                        "Neem je rijbewijs en identiteitskaart mee.",
                rent.getCar().getBrand(),
                rent.getCar().getModel());

        createAndSendNotification(rent.getRenterEmail(), NotificationType.RENTAL_REMINDER,
                title, message, rent.getId(), "RENT");
    }

    public void sendReturnReminder(Rent rent) {
        String title = "Herinnering: Auto terugbrengen morgen";
        String message = String.format(
                "Vergeet niet om morgen je gehuurde %s %s terug te brengen op tijd.",
                rent.getCar().getBrand(),
                rent.getCar().getModel());

        createAndSendNotification(rent.getRenterEmail(), NotificationType.RETURN_REMINDER,
                title, message, rent.getId(), "RENT");
    }

    public void notifyAccountStatusChange(String userEmail, boolean enabled) {
        NotificationType type = enabled ? NotificationType.ACCOUNT_ENABLED : NotificationType.ACCOUNT_DISABLED;
        String title = enabled ? "Account geactiveerd" : "Account gedeactiveerd";
        String message = enabled
                ? "Je account is geactiveerd. Je kunt nu weer inloggen en gebruik maken van alle functies."
                : "Je account is tijdelijk gedeactiveerd. Neem contact op met de beheerder voor meer informatie.";

        createAndSendNotification(userEmail, type, title, message, null, "USER");
    }

    // === USER NOTIFICATION MANAGEMENT ===

    public List<NotificationDTO> getUserNotifications(String userEmail) {
        List<Notification> notifications = notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(userEmail);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(String userEmail) {
        List<Notification> notifications = notificationRepository
                .findByRecipientEmailAndReadAtIsNullOrderByCreatedAtDesc(userEmail);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userEmail) {
        return notificationRepository.countUnreadByRecipientEmail(userEmail);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }

    public void markAllAsRead(String userEmail) {
        notificationRepository.markAllAsReadForUser(userEmail, LocalDateTime.now());
    }

    // === ADMIN FUNCTIONS ===

    public void broadcastSystemAnnouncement(String title, String message) {
        // Verstuur naar alle actieve gebruikers
        // Je zou hier een query kunnen maken om alle enabled users op te halen
        // Voor nu een placeholder
    }

    public void cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);
        notificationRepository.deleteAll(oldNotifications);
    }

    // === HELPER METHODS ===

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getType(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
