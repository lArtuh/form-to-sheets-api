package com.backend.form_to_sheets_api.service;

import com.backend.form_to_sheets_api.component.FormMapper;
import com.backend.form_to_sheets_api.dto.FormRequestDto;
import com.backend.form_to_sheets_api.model.FormModel;
import com.backend.form_to_sheets_api.exception.GoogleSheetsException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class FormService {

    private final SheetsService sheetsService;
    private final TurnstileService turnstileService;
    private final AntibotService antibotService;
    private final FormMapper formMapper;
    private final IdService idService;
    private final EmailService emailService;


    public ResponseEntity<String> processVolunteerApplication(FormRequestDto requestDto, HttpServletRequest request) {


            String clientIp = antibotService.extractClientIp(request);

            // 1. Validar el token de Turnstile
//            boolean isValid = turnstileService.verifyToken(requestDto.getTurnstileToken(), clientIp);
//
//            if (!isValid) {
//                // Si falla el anti-bot, retornamos un 400 Bad Request controlado
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("Anti-bot validation failed. Please try again.");
//            }

            // Enriquecemos con un id único usando UUID
            String uniqueId = idService.createId();

            // 2. Enriquecemos con fecha y hora actual de registro
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 3. Pasamos los datos del DTO al modelo
            FormModel formModel  = formMapper.toModel(requestDto, currentDateTime, uniqueId);

            // 4. Llamar al servicio específico de Sheets para guardar la información allá
        try {
            sheetsService.appendRowToSheet(formModel);
        } catch (IOException | GeneralSecurityException e) {
            // 1. Atrapamos la excepción "molesta" de Java
            // 2. La envolvemos en tu propia excepción de runtime que sí lee el manejador global
            throw new GoogleSheetsException("Error al conectar con Google Sheets", e);
        }

            // 5. Llamar al servicio específico EmailNotification (cliente y admin)
            emailService.sendBookingNotificationClient2(requestDto.getEmail(),requestDto.getFullName());
            emailService.sendBookingNotificationAdmin(requestDto.getEmail(),requestDto.getFullName());

            return ResponseEntity.ok("Registration successful and saved to Google Sheets!");


    }
}