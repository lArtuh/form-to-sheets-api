package com.backend.form_to_sheets_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${app.admin.email}")
    private String adminEmail;

    public void sendBookingNotification(String toEmail, String fullName) {
        try {
            // 1. Correo para el cliente (usuario)
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("¡Registration successfully completed.!");
            message.setText("Hi " + fullName + ",\n\n" +
                    "We have successfully received your registration, and it has been saved in our database");

            mailSender.send(message);


            // 2. Correo para el administrador (en inglés, usando la propiedad inyectada)
            SimpleMailMessage adminMessage = new SimpleMailMessage();
            adminMessage.setTo(adminEmail);
            adminMessage.setSubject("New registration received: " + fullName);
            adminMessage.setText("Hello Administrator,\n\n" +
                    "A new user has registered in the system:\n" +
                    "- Name: " + fullName + "\n" +
                    "- Email: " + toEmail + "\n\n" +
                    "Please check your Google Sheet to see all the details.");

            mailSender.send(adminMessage);


        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo electrónico: " + e.getMessage());
        }
    }
}