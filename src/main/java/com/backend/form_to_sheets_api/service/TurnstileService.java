package com.backend.form_to_sheets_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TurnstileService {

    @Value("${app.cloudflare.turnstile.secret}")
    private String secretKey;

    @Value("${app.cloudflare.turnstile.url}")
    private String verifyUrl;

    public boolean verifyToken(String token, String remoteIp) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();

        // Configuramos los parámetros que exige Cloudflare
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secret", secretKey);
        requestBody.add("response", token);
        if (remoteIp != null && !remoteIp.isEmpty()) {
            requestBody.add("remoteip", remoteIp);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Hacemos la petición POST a Cloudflare
            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Cloudflare responde con un JSON que contiene un booleano llamado "success"
                return Boolean.TRUE.equals(response.getBody().get("success"));
            }
        } catch (Exception e) {
            // Si falla la conexión con Cloudflare, por seguridad denegamos o manejamos el error
            return false;
        }

        return false;
    }
}
