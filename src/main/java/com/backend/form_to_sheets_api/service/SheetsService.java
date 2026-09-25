package com.backend.form_to_sheets_api.service;

import com.backend.form_to_sheets_api.dto.FormRequestDto;
import com.backend.form_to_sheets_api.model.FormModel;
import org.springframework.stereotype.Service;

@Service
public class SheetsService {

    // Aquí iría la configuración de Google Sheets API (Credentials, Sheets client, etc.)

    public void appendRowToSheet(FormModel data) {
        try {
            // Lógica para conectarse a Google Sheets API
            // 1. Autenticar con el archivo JSON de credenciales de Google Cloud
            // 2. Definir el Spreadsheet ID y el rango (ej: "Hoja1!A:G")
            // 3. Mapear el DTO a una lista de valores [nombre, email, teléfono, ciudad, motivación, fecha...]
            // 4. Enviar la petición append a Google Sheets

            System.out.println("Enviando datos a Google Sheets para: " + data.getName());

        } catch (Exception e) {
            throw new RuntimeException("Error al registrar los datos en Google Sheets: " + e.getMessage());
        }
    }
}