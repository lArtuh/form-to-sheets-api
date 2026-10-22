package com.backend.form_to_sheets_api.service;

import com.backend.form_to_sheets_api.model.FormModel;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;


import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    @Value("${app.sheets.filename}")
    private String sheetsFileName;

    private final GoogleApiService googleApiService;

    @Value("${app.emptyText}")
    private String emptyText;

    // Método auxiliar privado para obtener dinámicamente el total de columnas basadas en FormModel
    private int getTotalColumnsCount() {
        // Contamos los campos de FormModel + 1 extra que agregas manualmente para el uniqueId
        return FormModel.class.getDeclaredFields().length + 1;
    }

    // Método único que busca el archivo y te devuelve su ID (o null si no existe)
    public String getOrCreateSpreadsheetId() throws IOException, GeneralSecurityException {
        Drive driveService = googleApiService.getDriveService();

        String query = "name = '" + sheetsFileName + "' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false and sharedWithMe = true";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setIncludeItemsFromAllDrives(true)
                .setSupportsAllDrives(true)
                // Esto es clave para que busque en lo que te han compartido
                .setCorpora("user")
                .execute();

        // Si la lista tiene elementos, devolvemos el ID del primero
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        // Si no existe, devolvemos null
        return null;
    }


    // Método para crear el archivo
    public String createSpreadsheetId() throws IOException, GeneralSecurityException {
        // 1. Inicializar el servicio de Google Drive
        com.google.api.services.drive.Drive driveService = googleApiService.getDriveService();

        com.google.api.services.sheets.v4.model.Spreadsheet spreadsheet = new com.google.api.services.sheets.v4.model.Spreadsheet()
                .setProperties(new com.google.api.services.sheets.v4.model.SpreadsheetProperties().setTitle(sheetsFileName));

        com.google.api.services.sheets.v4.Sheets sheetsService = googleApiService.getSheetsService();
        com.google.api.services.sheets.v4.model.Spreadsheet createdSpreadsheet = sheetsService.spreadsheets().create(spreadsheet).execute();

        String newSpreadsheetId = createdSpreadsheet.getSpreadsheetId();

        // 5. Opcional: Crear automáticamente la primera fila con los nombres de las columnas (Cabeceras)
        initializeHeaders(newSpreadsheetId);

        return newSpreadsheetId;
    }

    //Método que escribe los datos en la tabla
    public void appendData(String spreadsheetId,String range, ValueRange body) throws IOException, GeneralSecurityException {

        Sheets sheetsService = googleApiService.getSheetsService();

        AppendValuesResponse sheets =sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
        // Ajustar el tamaño de las celdas
        autoResizeColumns(spreadsheetId,getTotalColumnsCount());

    }
    //Método que escribe los headers de la tabla
    public void initializeHeaders(String spreadsheetId) throws IOException, GeneralSecurityException {
        Sheets sheetsService = googleApiService.getSheetsService();

        Field[] fields = FormModel.class.getDeclaredFields();
        List<Object> headers = Arrays.stream(fields)
                .map(Field::getName)
                // 1. Inserta un espacio antes de cada mayúscula que siga a una minúscula
                .map(name -> name.replaceAll("([a-z])([A-Z])", "$1 $2"))
                // 2. Convierte la primera letra en mayúscula (ej: "full Name" -> "Full Name")
                .map(name -> name.substring(0, 1).toUpperCase() + name.substring(1))
                .collect(Collectors.toList());

        ValueRange body = new ValueRange().setValues(Collections.singletonList(headers));

        // Lo escribimos estrictamente en la primera fila (A1)
        UpdateValuesResponse sheets = sheetsService.spreadsheets().values()
                .update(spreadsheetId, getFirstSheetInfo(spreadsheetId).name +"!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        // Centramos y ajustamos las tablas
        centerCells(spreadsheetId,getTotalColumnsCount());
    }

    // Método para verificar si los headings coinciden ( devuelve true si son iguales, false si son diferentes o si está vacío)
    public boolean areHeadingsEquals(String spreadsheetId) throws IOException, GeneralSecurityException {

        // Extraemos los nombres de los atributos de la clase FormModel usando Reflexión
        Field[] fields = FormModel.class.getDeclaredFields();
        List<Object> headers = Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.toList());

        // Extraemos los valores de los atributos de la tabla
        Sheets sheetsServiceInstance = googleApiService.getSheetsService();
        SheetInfo sheetInfo = getFirstSheetInfo(spreadsheetId); // El nombre de la pestaña

        // Traemos la primera fila completa
        String headerRange = sheetInfo.name + "!1:1";
        ValueRange headerResponse = sheetsServiceInstance.spreadsheets().values()
                .get(spreadsheetId, headerRange)
                .execute();

        List<List<Object>> values = headerResponse.getValues();


        if (values == null || values.isEmpty()) {
            return false;
        }

        List<Object> rawRows = values.get(0);

        //Convertir la fila de Google a List<String> para poder compararla con 'headers'
        List<String> sheetHeaders = rawRows.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

    return headers.equals(sheetHeaders);

    }


    // Método que berifica si un correo electrónico ya existe registrado en la hoja de Google Sheets.
    public boolean isEmailAlreadyRegistered(String emailToFind, String spreadsheetId)
            throws IOException, GeneralSecurityException {
        if (emailToFind == null || emailToFind.trim().isEmpty()) {
            return false;
        }


        Sheets sheetsServiceInstance = googleApiService.getSheetsService();
        SheetInfo sheetInfo = getFirstSheetInfo(spreadsheetId); // El nombre de tu pestaña

        // 1. Traemos la primera fila completa para encontrar en qué columna está "Email"
        String headerRange = sheetInfo.name + "!1:1";
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
            if (headerTitle.equalsIgnoreCase("Email")) {
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
        String dataRange = sheetInfo.name + "!" + columnLetter + "2:" + columnLetter; //Hoja1!B2:B
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
                if (registeredEmail.trim().equalsIgnoreCase(emailToFind.trim())) {
                    return true; // ¡Coincidencia encontrada!
                }
            }
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


    // Método para obtener toda la información del sheets (nombre e id)
    public record SheetInfo(String name, Integer sheetId) {}
    public SheetInfo getFirstSheetInfo(String spreadsheetId) throws IOException, GeneralSecurityException {
        Sheets sheetsServiceInstance = googleApiService.getSheetsService();

        // 1. Una sola llamada HTTP a Google Sheets
        com.google.api.services.sheets.v4.model.Spreadsheet spreadsheet =
                sheetsServiceInstance.spreadsheets().get(spreadsheetId).execute();

        var sheets = spreadsheet.getSheets();
        if (sheets != null && !sheets.isEmpty()) {
            var properties = sheets.get(0).getProperties();

            // 2. Empaquetamos tanto el nombre como el ID en nuestro objeto
            return new SheetInfo(properties.getTitle(), properties.getSheetId());
        }

        throw new IllegalStateException("El archivo de Google Sheets no contiene ninguna pestaña.");
    }
    // Método para ajustar el tamaño de las celdas usadas
    public void autoResizeColumns(String spreadsheetId, int sheetsAmount) throws IOException, GeneralSecurityException {

        SheetInfo sheetInfo = getFirstSheetInfo(spreadsheetId);

        // 1. Creamos el rango de dimensiones directamente con DimensionRange
        DimensionRange dimensionRange = new DimensionRange()
                .setSheetId(sheetInfo.sheetId)
                .setDimension("COLUMNS") // Indicamos que se ajustarán columnas
                .setStartIndex(0)         // Desde la columna A (índice 0)
                .setEndIndex(sheetsAmount);           // Hasta la columna G (índice exclusivo 7)

        // 2. Pasamos el rango usando el método correcto .setDimensions()
        AutoResizeDimensionsRequest autoResizeRequest = new AutoResizeDimensionsRequest()
                .setDimensions(dimensionRange);

        // 3. Empaquetamos la solicitud en el objeto Request general
        Request request = new Request().setAutoResizeDimensions(autoResizeRequest);

        BatchUpdateSpreadsheetRequest batchUpdateBody = new BatchUpdateSpreadsheetRequest()
                .setRequests(List.of(request));

        // 4. Ejecutamos la petición contra la API de Google Sheets
        googleApiService.getSheetsService().spreadsheets()
                .batchUpdate(spreadsheetId, batchUpdateBody)
                .execute();
    }
    // Método para centrar el contenido de las celdas usadas
    public void centerCells(String spreadsheetId, int sheetsAmount) throws IOException, GeneralSecurityException {

        SheetInfo sheetInfo = getFirstSheetInfo(spreadsheetId);

        // 1. Definimos la regla de formato para centrar el texto
        CellFormat centerFormat = new CellFormat()
                .setHorizontalAlignment("CENTER")
                .setVerticalAlignment("MIDDLE");

        // 2. Creamos la celda con el formato aplicado
        CellData cellData = new CellData()
                .setUserEnteredFormat(centerFormat);

        // 3. Definimos el rango exacto al que le aplicaremos el cambio usando GridRange
        GridRange range = new GridRange()
                .setSheetId(sheetInfo.sheetId)
                .setStartRowIndex(0)
                .setEndRowIndex(1)
                .setStartColumnIndex(0)
                .setEndColumnIndex(sheetsAmount);

        // 4. Creamos la petición de repetición de celdas
        RepeatCellRequest repeatCellRequest = new RepeatCellRequest()
                .setRange(range)
                .setCell(cellData)
                .setFields("userEnteredFormat(horizontalAlignment,verticalAlignment)");

        // 5. Empaquetamos en el Request general de BatchUpdate
        Request request = new Request().setRepeatCell(repeatCellRequest);
        BatchUpdateSpreadsheetRequest batchBody = new BatchUpdateSpreadsheetRequest()
                .setRequests(List.of(request));

        // 6. Ejecutamos contra la API
        googleApiService.getSheetsService().spreadsheets()
                .batchUpdate(spreadsheetId, batchBody)
                .execute();
    }

    // Método para mapear los datos de tu FormModel a una lista de objetos para Google Sheets
    public List<Object> mapFormToRowData(FormModel form) {
        return List.of(
                form.getId() != null ? form.getId() : emptyText,
                form.getFullName() != null ? form.getFullName() : emptyText,
                form.getNameOfOrganization() != null ? form.getNameOfOrganization() : emptyText,
                form.getPosition() != null ? form.getPosition() : emptyText,
                form.getAddress() != null ? form.getAddress() : emptyText,
                form.getPhone() != null ? form.getPhone() : emptyText,
                form.getEmail() != null ? form.getEmail() : emptyText,
                form.getMembershipType() != null ? form.getMembershipType() : emptyText,
                form.getFullAlternateName() != null ? form.getFullAlternateName() : emptyText,
                form.getAlternatePosition() != null ? form.getAlternatePosition() : emptyText,
                form.getAlternatePhoneNumber() != null ? form.getAlternatePhoneNumber() : emptyText,
                form.getPaymentMethod() != null ? form.getPaymentMethod() : emptyText,
                form.getContactSharingConsent() != null ? form.getContactSharingConsent() : emptyText,
                form.getExternalOrgSharingConsent() != null ? form.getExternalOrgSharingConsent() : emptyText,
                form.getOrganizationInfo() != null ? form.getOrganizationInfo() : emptyText,
                form.getEntreUsageCapacity() != null ? form.getEntreUsageCapacity() : emptyText,
                form.getOfficeWaitingListConsent() != null ? form.getOfficeWaitingListConsent() : emptyText,
                form.getRequireBuildingKey() != null ? form.getRequireBuildingKey() : emptyText,
                form.getDigitalSignature() != null ? form.getDigitalSignature() : emptyText,
                form.getRegistrationDate() != null ? form.getRegistrationDate() : emptyText
        );
    }

}
