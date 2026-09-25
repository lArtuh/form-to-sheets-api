package com.backend.form_to_sheets_api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FormRequestDto {
    @NotBlank(message = "Full name is mandatory")
    @Size(min = 3, max = 100, message = "The name must be between 3 and 100 characters.")
    private String fullName;

    @NotBlank(message = "email is mandatory")
    @Email( message = "You must provide a valid email format.")
    private String email;


    private Integer phone;
    private LocalDateTime birthdate;
    private String resume;

    //constructor vacío
    public FormRequestDto() {}

    //constructor con parámetros
    public FormRequestDto(String fullName, String email, Integer phone,
                               LocalDateTime birthdate, String resume) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.birthdate = birthdate;
        this.resume = resume;

    }


    //geters

    public String getName (){
        return fullName;
    }
    public String getEmail (){
        return email;
    }
    public Integer getPhone (){
        return phone;
    }
    public LocalDateTime getBirthdate (){
        return birthdate;
    }
    public String getResume (){
        return resume;
    }

    //seters

    public void setName (String name){
        this.fullName = name;
    }

    public void setBirthday (LocalDateTime birthday) {
        this.birthdate = birthday;
    }

    public void setEmail (){
        this.email = email;
    }

    public void setPhone (){
        this.phone = phone;
    }

    public void setBirthdate (){
        this.birthdate = birthdate;
    }

    public void setResume (){
        this.resume = resume;
    }
}
