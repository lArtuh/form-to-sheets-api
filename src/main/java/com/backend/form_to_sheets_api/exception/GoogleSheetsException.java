package com.backend.form_to_sheets_api.exception;


public class GoogleSheetsException extends RuntimeException {
    public GoogleSheetsException(String message) {
        super(message);
    }

    public GoogleSheetsException(String message, Throwable cause) {
        super(message, cause);
    }
}