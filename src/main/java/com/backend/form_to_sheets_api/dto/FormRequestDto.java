package com.backend.form_to_sheets_api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FormRequestDto {
    @NotBlank(message = "Full name is mandatory")
    @Size(min = 3, max = 100, message = "The name must be between 3 and 100 characters.")
    private String fullName;

    @NotBlank(message = "email is mandatory")
    @Email( message = "You must provide a valid email format.")
    private String email;


    private String phone;
    private LocalDateTime birthdate;
    private String resume;

    private String turnstileToken;

}
