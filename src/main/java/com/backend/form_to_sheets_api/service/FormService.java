package com.backend.form_to_sheets_api.service;

import com.backend.form_to_sheets_api.dto.FormRequestDto;
import com.backend.form_to_sheets_api.model.FormModel;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FormService {

    private final SheetsService googleSheetsService;


    // Inyectamos el servicio específico de Google Sheets
    public FormService(SheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    public void processVolunteerApplication(FormRequestDto requestDto) {
        // 1. Opcional: Podrías enriquecer los datos, por ejemplo, agregando la fecha y hora actual de registro
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. Pasamos los datos del DTO al modelo (o los enriquecemos)
        // 2. falta agregar el datatime
        FormModel formModel = new FormModel();
        formModel.setName(requestDto.getName());
        formModel.setEmail(requestDto.getEmail());
        formModel.setPhone(requestDto.getPhone());
        formModel.setBirthdate(requestDto.getBirthdate());
        formModel.setResume(requestDto.getResume());

        // 3. Llamar al servicio específico de Sheets para guardar la información allá
        googleSheetsService.appendRowToSheet(formModel);

        // Aquí podrías agregar más llamadas a otros servicios generales (ej: EmailService)
    }
}
