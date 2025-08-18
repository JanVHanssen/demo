package be.ucll.se.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import be.ucll.se.demo.model.Notification;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from:noreply@car4rent.be}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    public void sendNotificationEmail(Notification notification) {
        if (!emailEnabled) {
            System.out.println("📧 Email disabled - skipping notification for: " + notification.getRecipientEmail());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getTitle());

            String htmlContent = buildEmailTemplate(notification);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            // Success logging
            System.out.println("✅ Email sent successfully to: " + notification.getRecipientEmail() +
                    " | Subject: " + notification.getTitle());

        } catch (Exception e) {
            // Better error logging
            System.err.println("❌ Failed to send email to: " + notification.getRecipientEmail() +
                    " | Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    private String buildEmailTemplate(Notification notification) {
        return String.format(
                """
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>%s</title>
                        </head>
                        <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f5f5f5;">
                            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px;">
                                <div style="text-align: center; margin-bottom: 20px;">
                                    <h1 style="color: #007bff; margin: 0; font-size: 24px;">🚗 Auto Verhuur</h1>
                                </div>
                                <h2 style="color: #333; margin-bottom: 20px; font-size: 20px;">%s</h2>
                                <div style="background-color: white; padding: 20px; border-radius: 8px; border-left: 4px solid #007bff; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                    <p style="color: #666; line-height: 1.6; margin: 0; font-size: 16px;">%s</p>
                                </div>
                                <div style="margin-top: 30px; text-align: center; border-top: 1px solid #eee; padding-top: 20px;">
                                    <p style="color: #999; font-size: 12px; margin: 0;">
                                        Dit is een automatisch gegenereerde email van Auto Verhuur Systeem
                                    </p>
                                    <p style="color: #999; font-size: 11px; margin: 5px 0 0 0;">
                                        Type: %s | Verzonden: %s
                                    </p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                notification.getTitle(), // Voor <title> tag
                notification.getTitle(), // Voor <h2>
                notification.getMessage(), // Voor content
                notification.getType() != null ? notification.getType().toString() : "SYSTEM",
                notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : "Nu");
    }

    // Extra method voor testing
    public void sendTestEmail(String toEmail) {
        if (!emailEnabled) {
            System.out.println("📧 Email disabled - test email skipped");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 Auto Verhuur - Test Email");

            String htmlContent = """
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    </head>
                    <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f5f5f5;">
                        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px;">
                            <div style="text-align: center; margin-bottom: 20px;">
                                <h1 style="color: #007bff; margin: 0; font-size: 24px;">🚗 Auto Verhuur Demo</h1>
                            </div>
                            <h2 style="color: #333; margin-bottom: 20px;">Test Email Succesvol! 🎉</h2>
                            <div style="background-color: white; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                <p style="color: #666; line-height: 1.6; margin: 0 0 15px 0;">
                                    <strong>Gefeliciteerd!</strong> Je email configuratie werkt perfect.
                                </p>
                                <p style="color: #666; line-height: 1.6; margin: 0;">
                                    Het notificatiesysteem is nu klaar om gebruikt te worden voor:
                                </p>
                                <ul style="color: #666; margin: 10px 0;">
                                    <li>🔔 Nieuwe boekingen</li>
                                    <li>✅ Boekingsbevestigingen</li>
                                    <li>❌ Annuleringen</li>
                                    <li>⏰ Herinneringen</li>
                                </ul>
                            </div>
                            <div style="margin-top: 30px; text-align: center; border-top: 1px solid #eee; padding-top: 20px;">
                                <p style="color: #999; font-size: 12px; margin: 0;">
                                    Dit is een test email via Mailtrap
                                </p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """;

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("✅ Test email sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send test email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send test email", e);
        }
    }
}