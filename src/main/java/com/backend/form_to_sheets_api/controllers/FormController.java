package com.backend.form_to_sheets_api.controllers;

import com.backend.form_to_sheets_api.dto.FormRequestDto;
import com.backend.form_to_sheets_api.service.FormService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/registration")
@CrossOrigin(origins = "*") // Permite peticiones desde tu frontend (ajustalo si prefieres seguridad estricta)
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;


    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody FormRequestDto formRequestDto,
                                               HttpServletRequest request) {

        return formService.processVolunteerApplication(formRequestDto, request);
    }
}
