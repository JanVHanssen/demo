package be.ucll.se.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import be.ucll.se.demo.service.NotificationService;

@RestController
@RequestMapping("/admin/notifications")
@CrossOrigin(origins = "*")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcastAnnouncement(
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        String message = request.get("message");
        notificationService.broadcastSystemAnnouncement(title, message);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldNotifications(
            @RequestParam(defaultValue = "30") int daysOld) {
        notificationService.cleanupOldNotifications(daysOld);
        return ResponseEntity.ok().build();
    }
}