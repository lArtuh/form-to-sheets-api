package com.backend.form_to_sheets_api.service;

import com.backend.form_to_sheets_api.dto.FormRequestDto;
import com.backend.form_to_sheets_api.model.FormModel;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormService {

    private final SheetsService sheetsService;
    private final EmailService emailService;
    private final TurnstileService turnstileService;


    public void processVolunteerApplication(FormRequestDto requestDto, String clientIp) {

        // 1. Validar el token de Turnstile
        boolean isValid = turnstileService.verifyToken(requestDto.getTurnstileToken(), clientIp);

        if (!isValid) {
            throw new SecurityException("Anti-bot validation failed. Please try again.");
        }

        // 2. Opcional: Podrías enriquecer los datos, por ejemplo, agregando la fecha y hora actual de registro
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 3. Pasamos los datos del DTO al modelo (o los enriquecemos)
        //  falta agregar el datatime
        FormModel formModel = new FormModel();
        formModel.setFullname(requestDto.getFullName());
        formModel.setEmail(requestDto.getEmail());
        formModel.setPhone(requestDto.getPhone());
        formModel.setBirthdate(requestDto.getBirthdate());
        formModel.setResume(requestDto.getResume());

        // 4. Llamar al servicio específico de Sheets para guardar la información allá
        sheetsService.appendRowToSheet(formModel);

        // 5. Llamar al servicio específico EmailNotification
        emailService.sendBookingNotification(requestDto.getEmail(),requestDto.getFullName());
    }
}
