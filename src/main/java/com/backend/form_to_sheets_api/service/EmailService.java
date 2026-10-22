package com.backend.form_to_sheets_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("#{'${app.admin.email}'.split(',')}")
    private List<String> adminEmail;

    @Value("${app.sheets.filename}")
    private String nameFile;

    // Método para enviar correo al cliente
    public void sendBookingNotificationClient(String toEmail, String fullName) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Voluntary Resource Centre - Registration successfully completed!");
            message.setText("Hi " + fullName + ",\n\n" +
                    "We have successfully received your registration, and it has been saved in our database."+ "\n\n" +
                            "Voluntary Resource Centre"+ "\n\n" +
                            "81 Prince St"+ "\n\n" +
                            "902-368-7337"
                    );

            mailSender.send(message);
        } catch (Exception e) {
            // Aquí solo registras el error (en un log real usarías un logger)
            // pero dejas que el flujo continúe para avisarle al admin
            System.err.println("No se pudo enviar el correo al cliente (" + toEmail + "): " + e.getMessage());
        }
    }

    // Método para enviar correo al cliente (con html)
    public void sendBookingNotificationClient2(String toEmail, String fullName) {
        try {
            // 1. Crear el MimeMessage para soportar contenido HTML
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Voluntary Resource Centre - Registration successfully completed!");

            // 2. Maquetación HTML alineada a los colores y estilo de la web
            String htmlContent = """
<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #faf7f1;
            margin: 0;
            padding: 0;
        }

        .email-container {
            max-width: 600px;
            margin: 30px auto;
            background: #faf7f1;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
            border: 1px solid #eae8e4;
        }

        .email-header {
            background-color: #faf7f1;
            padding: 25px;
            text-align: center;
            border-bottom: 3px solid #f4c430;
            /* Tonalidad amarilla distintiva */
        }

        .email-header h1 {
            color: #2c3e50;
            font-size: 20px;
            margin: 0;
            font-family: Georgia, serif;
            /* Inspirado en los títulos de la web */
        }

        .email-body {
            padding: 40px 35px;
            color: #4a4a4a;
            font-size: 16px;
            line-height: 1.7;
            font-family: Arial, Helvetica, sans-serif;
            /* <--- Se aplica a todo el bloque aquí */
        }

        .email-footer {
            background-color: #f3ede4;
            padding: 20px;
            text-align: center;
            font-size: 14px;
            color: #777777;
            border-top: 1px solid #eae8e4;
        }

        .highlight {
            color: #2c3e50;
            font-weight: bold;
        }
    </style>
</head>

<body>
    <div class="email-container">
        <!-- Cabecera -->
        <div class="email-header">
            <h1
                style="color: #636053; font-size: 40px; margin: 0; font-family: 'Newsreader', Georgia, 'Times New Roman', serif; font-weight: 10;">
                Registration successfully completed!

            </h1>

        </div>

        <!-- Contenido Principal -->
        <div class="email-body">
            <p style="margin-top: 0;">Hi %s,</p>

            <p>We have successfully received your registration, and it has been saved in our database.</p>

            <p>Thank you for getting involved and helping us build a stronger community together!</p>
        </div>

        <!-- Pie de página con los datos de contacto -->
        <div class="email-footer">
            <p style="margin: 0 0 6px 0; font-size: 16px; color: #636053; line-height: 1.6;">
                <strong>Voluntary Resource Centre</strong>
            </p>
            <p style="margin: 0 0 6px 0; font-size: 16px; color: #636053; font-weight: 400; line-height: 1.5;">
                81 Prince Street Charlottetown
            </p>
            <p style="margin: 0 0 6px 0; font-size: 16px; color: #636053; font-weight: 400; line-height: 1.5;">
                902-368-7337
            </p>
        </div>
    </div>
</body>

</html>
            """.formatted(fullName); // Remplaza %s con el nombre del usuario de forma limpia

            // El 'true' indica que es multipart/HTML
            helper.setText(htmlContent, true);

            // 3. Enviar el correo
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("Error al construir el correo HTML para el cliente (" + toEmail + "): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo al cliente (" + toEmail + "): " + e.getMessage());
        }
    }

    // Método para enviar correo al administrador
        public void sendBookingNotificationAdmin(String toEmail, String fullName) {
        // 2. Intentar enviar el correo al administrador (independiente del anterior)
        try {
            for (String singleAdminEmail : adminEmail) {
                SimpleMailMessage adminMessage = new SimpleMailMessage();
                adminMessage.setTo(singleAdminEmail);
                adminMessage.setSubject("Automatic Registration Service - New registration received: " + fullName);
                adminMessage.setText("Hello Administrator,\n\n" +
                        "A new user has registered in the system:\n" +
                        "- Name: " + fullName + "\n" +
                        "- Email: " + toEmail + "\n\n" +
                        "Please check your Google Sheet file named '" + nameFile + "' to see all the details.");

                mailSender.send(adminMessage);
            }
        } catch (Exception e) {
            // Si el del administrador también falla, aquí sí podrías lanzar una excepción
            // o manejarlo según qué tan crítico consideres este aviso.
            System.err.println("Error al enviar el correo al administrador: " + e.getMessage());
            throw new RuntimeException("Error crítico al notificar al administrador: " + e.getMessage());
        }
    }
}