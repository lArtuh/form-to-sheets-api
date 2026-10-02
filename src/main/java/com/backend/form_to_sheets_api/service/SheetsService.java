package com.backend.form_to_sheets_api.service;
import com.backend.form_to_sheets_api.model.FormModel;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.lang.reflect.Field;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Service
@RequiredArgsConstructor
@Getter
public class SheetsService {


    // Ya no son static final, ahora son inyectadas por Spring
    @Value("${app.name}")
    private String applicationName;

    @Value("${app.sheets.filename}")
    private String sheetsFileName;

    private final GoogleApiService googleApiService;

    // Método para guardar la información
    public void appendRowToSheet(FormModel form) {
        try {
            // 1. Obtener o crear automáticamente el ID usando el método de esta misma clase
            String spreadsheetId = getOrCreateSpreadsheetId();

            // 2. Inicializar el servicio de Sheets usando el servicio inyectado
            Sheets sheetsService = googleApiService.getSheetsService();

            // 3. Definir en qué rango o pestaña se insertarán los datos (ej: "Hoja1!A")
            String range = "Hoja1!A";

            // 4. Mapear los datos de tu FormModel a una lista de objetos
            List<Object> rowData = List.of(
                    form.getFullname(),
                    form.getEmail(),
                    form.getBirthdate() != null ? form.getBirthdate().toString() : ""
            );

            ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));

            // 5. Ejecutar la petición de inserción (APPEND)
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Error al comunicarse con Google Sheets: " + e.getMessage());
        }

    }

    // Crear el archivo (si no existe)
    private String getOrCreateSpreadsheetId() throws IOException, GeneralSecurityException {
        // 1. Inicializar el servicio de Google Drive
        com.google.api.services.drive.Drive driveService = googleApiService.getDriveService();

        // 2. Buscar si ya existe un archivo con ese nombre en el Drive
        String query = "name = '" + sheetsFileName + "' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false";
        com.google.api.services.drive.model.FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .execute();

        // 3. Si ya existe, retornamos su ID para usarlo
        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        // 4. Si NO existe, lo creamos desde cero
        com.google.api.services.sheets.v4.model.Spreadsheet spreadsheet = new com.google.api.services.sheets.v4.model.Spreadsheet()
                .setProperties(new com.google.api.services.sheets.v4.model.SpreadsheetProperties().setTitle(sheetsFileName));

        com.google.api.services.sheets.v4.Sheets sheetsService = googleApiService.getSheetsService();
        com.google.api.services.sheets.v4.model.Spreadsheet createdSpreadsheet = sheetsService.spreadsheets().create(spreadsheet).execute();

        String newSpreadsheetId = createdSpreadsheet.getSpreadsheetId();

        // 5. Opcional: Crear automáticamente la primera fila con los nombres de las columnas (Cabeceras)
        initializeHeaders(newSpreadsheetId);

        return newSpreadsheetId;
    }
    //Método que escribe los headers de la tabla
    private void initializeHeaders(String spreadsheetId) throws IOException, GeneralSecurityException {
        Sheets sheetsService = googleApiService.getSheetsService();

        // Extraemos los nombres de los atributos de la clase FormModel usando Reflexión
        Field[] fields = FormModel.class.getDeclaredFields();
        List<Object> headers = Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.toList());

        ValueRange body = new ValueRange().setValues(Collections.singletonList(headers));

        // Lo escribimos estrictamente en la primera fila (A1)
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, "Hoja1!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    // Método que berifica si un correo electrónico ya existe registrado en la hoja de Google Sheets.
    public boolean isEmailAlreadyRegistered(String emailToFind, String spreadsheetId) {
        if (emailToFind == null || emailToFind.trim().isEmpty()) {
            return false;
        }

        try {
            Sheets sheetsServiceInstance = googleApiService.getSheetsService();
            String sheetName = "Hoja 1"; // El nombre de tu pestaña

            // 1. Traemos la primera fila completa para encontrar en qué columna está "Email"
            String headerRange = sheetName + "!1:1";
            ValueRange headerResponse = sheetsServiceInstance.spreadsheets().values()
                    .get(spreadsheetId, headerRange)
                    .execute();

            List<Object> headers = headerResponse.getValues() != null && !headerResponse.getValues().isEmpty()
                    ? headerResponse.getValues().get(0)
                    : null;

            if (headers == null) {
                return false; // No hay encabezados
            }

            // 2. Buscamos el índice (número de columna) donde el encabezado sea "Email" o "Correo"
            int emailColumnIndex = -1;
            for (int i = 0; i < headers.size(); i++) {
                String headerTitle = headers.get(i).toString().trim();
                if (headerTitle.equalsIgnoreCase("Email") || headerTitle.equalsIgnoreCase("Correo")) {
                    emailColumnIndex = i;
                    break;
                }
            }

            if (emailColumnIndex == -1) {
                // Si no encuentra una columna llamada Email, asumimos que no está registrada aún
                return false;
            }

            // Convertimos el índice de la columna (ej. 0 -> A, 1 -> B) a letra para la consulta
            String columnLetter = getColumnLetter(emailColumnIndex);

            // 3. Consultamos únicamente los datos de esa columna específica (ej: "Hoja 1!B2:B")
            String dataRange = sheetName + "!" + columnLetter + "2:" + columnLetter;
            ValueRange dataResponse = sheetsServiceInstance.spreadsheets().values()
                    .get(spreadsheetId, dataRange)
                    .execute();

            List<List<Object>> rows = dataResponse.getValues();
            if (rows == null || rows.isEmpty()) {
                return false;
            }

            // 4. Recorremos los valores de la columna buscando el correo
            for (List<Object> row : rows) {
                if (!row.isEmpty()) {
                    String registeredEmail = row.get(0).toString();
                    if (registeredEmail.equalsIgnoreCase(emailToFind.trim())) {
                        return true; // ¡Coincidencia encontrada!
                    }
                }
            }

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Error al verificar el correo en Google Sheets", e);
        }

        return false;
    }

    // Método auxiliar pequeño para convertir el índice numérico (0, 1, 2...) a letra de columna (A, B, C...)
    private String getColumnLetter(int columnIndex) {
        StringBuilder columnLetter = new StringBuilder();
        while (columnIndex >= 0) {
            columnLetter.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnLetter.toString();
    }

}