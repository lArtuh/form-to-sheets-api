package com.backend.form_to_sheets_api.model;

import java.time.LocalDateTime;



public class FormModel {
    private long id;
    private String fullname;
    private String email;
    private Integer phone;
    private LocalDateTime birthdate;
    private String resume;

    //geters
    public Long getId (){
        return id;
    }

    public String getName (){
        return fullname;
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
    public void setId (Integer id){
        this.id = id;
    }

    public void setName (String name){
        this.fullname = name;
    }


    public void setEmail (String email){
        this.email = email;
    }

    public void setPhone (Integer phone){
        this.phone = phone;
    }

    public void setBirthdate (LocalDateTime birthdate){
        this.birthdate = birthdate;
    }

    public void setResume (String resume){
        this.resume = resume;
    }
}
