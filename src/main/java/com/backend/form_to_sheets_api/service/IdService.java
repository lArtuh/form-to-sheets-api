package com.backend.form_to_sheets_api.service;

import org.springframework.stereotype.Service;

@Service
public class IdService {
    public String createId() {
        return java.util.UUID.randomUUID().toString();
    }

}
