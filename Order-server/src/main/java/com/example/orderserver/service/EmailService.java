package com.example.orderserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderConfirmation(String toEmail, String orderId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("support@alkabhawy-gallery.com");
            message.setTo(toEmail);

            // כותרת המייל בעברית
            message.setSubject("אישור הזמנה מגלריית אל-קבהאווי - מספר הזמנה: #" + orderId);

            // תוכן המייל בעברית
            String emailBody = "שלום רב,\n\n" +
                    "ההזמנה שלך התקבלה בהצלחה בגלריית אל-קבהאווי ונמצאת כעת בטיפול.\n\n" +
                    "מספר ההזמנה שלך הוא: #" + orderId + "\n\n" +
                    "תודה רבה על האמון בגלריית אל-קבהאווי ונשמח לראותך שוב!";

            message.setText(emailBody);

            mailSender.send(message);
            System.out.println("Email sent successfully in Hebrew to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }
}