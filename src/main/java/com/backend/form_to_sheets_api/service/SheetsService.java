package com.backend.form_to_sheets_api.service;
import com.backend.form_to_sheets_api.model.FormModel;
import com.backend.form_to_sheets_api.exception.DuplicateEmailException;
import com.backend.form_to_sheets_api.exception.GoogleSheetsException;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Collections;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.beans.factory.annotation.Value;


import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Service
@RequiredArgsConstructor
@Getter
public class SheetsService {


    @Value("${app.name}")
    private String applicationName;

    @Value("${app.sheets.filename}")
    private String sheetsFileName;

    private final GoogleApiService googleApiService;
    private final TableService tableService;
    private final IdService idService;

    // Método para guardar la información
    public void appendRowToSheet(FormModel form) throws IOException, GeneralSecurityException {

        try {

            // 1. Intentamos obtener el ID del archivo existente
            String spreadsheetId = tableService.getOrCreateSpreadsheetId();

            // 2. Si es null, significa que no existe y debemos crearlo
            if (spreadsheetId == null) {
                throw new IllegalStateException("Failed to find or create the Google Spreadsheet.");
            } else {
                // 3. Si el archivo ya existía, verificamos si sus cabeceras coinciden con tu modelo
                boolean areHeadingsEquals = tableService.areHeadingsEquals(spreadsheetId);
             // 4. Si existen pero las cabeceras cambiaron/no coinciden, las actualizamos y las acomodamos
                if (!areHeadingsEquals) {
                    tableService.initializeHeaders(spreadsheetId);
                }
            }

            // validar que no se use el correo no esté en uso
            if (tableService.isEmailAlreadyRegistered(form.getEmail(),spreadsheetId)) {
                throw new DuplicateEmailException("The email address is already registered.");
            }

            // 6. Definir en qué rango o pestaña se insertarán los datos (ej: "Hoja1!A")
            String range = "'" + tableService.getFirstSheetInfo(spreadsheetId).name() + "'!A:A";


            // 7. Mapear los datos de tu FormModel a una lista de objetos
            List<Object> rowData =  tableService.mapFormToRowData(form);

            // Preparamos el cuerpo (payload) que la API de Google Sheets exige para enviar los datos
            ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));

            // 8. Ejecutar la petición de inserción (APPEND)
            tableService.appendData(spreadsheetId, range, body);

    } catch (IOException | GeneralSecurityException e) {
        // Aquí es donde la 'e' cobra vida: atrapamos el error técnico de Java y lo envolvemos
        throw new GoogleSheetsException("Error al conectar o interactuar con Google Sheets", e);
    }
        }
    }

