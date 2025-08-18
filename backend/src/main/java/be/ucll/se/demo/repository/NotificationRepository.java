package be.ucll.se.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ucll.se.demo.model.Notification;
import be.ucll.se.demo.model.NotificationStatus;
import be.ucll.se.demo.model.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Alle notificaties voor een gebruiker
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

    // Ongelezen notificaties
    List<Notification> findByRecipientEmailAndReadAtIsNullOrderByCreatedAtDesc(String email);

    // Notificaties per type
    List<Notification> findByRecipientEmailAndType(String email, NotificationType type);

    // Notificaties per status
    List<Notification> findByStatus(NotificationStatus status);

    // Notificaties ouder dan bepaalde datum (voor cleanup)
    List<Notification> findByCreatedAtBefore(LocalDateTime date);

    // Aantal ongelezen notificaties
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientEmail = :email AND n.readAt IS NULL")
    long countUnreadByRecipientEmail(@Param("email") String email);

    // Markeer als gelezen
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt, n.status = 'READ' WHERE n.id = :id")
    int markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    // Markeer alle als gelezen voor gebruiker
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt, n.status = 'READ' WHERE n.recipientEmail = :email AND n.readAt IS NULL")
    int markAllAsReadForUser(@Param("email") String email, @Param("readAt") LocalDateTime readAt);
}