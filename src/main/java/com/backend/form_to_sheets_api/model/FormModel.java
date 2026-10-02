package com.backend.form_to_sheets_api.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;



@Getter @Setter
public class FormModel {
    private long id;
    private String fullname;
    private String email;
    private String phone;
    private LocalDateTime birthdate;
    private String resume;



}
