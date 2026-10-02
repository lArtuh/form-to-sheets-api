package com.backend.form_to_sheets_api.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;



@Service
@RequiredArgsConstructor
public class GoogleApiService {

    private final SheetsService sheetsService;

    // Método para obtener el servicio de Google Sheets
    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        // Debes colocar tu archivo JSON de credenciales en la carpeta src/main/resources/
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ClassPathResource("credentials.json").getInputStream())
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(sheetsService.getApplicationName())
                .build();
    }
    // Método para obtener el servicio de Google Drive
    public Drive getDriveService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = getCredentials();

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(sheetsService.getApplicationName())
                .build();
    }
    // Método auxiliar para centralizar las credenciales con permisos de Sheets y Drive
    public GoogleCredentials getCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(new ClassPathResource("credentials.json").getInputStream())
                .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE));
    }

}
