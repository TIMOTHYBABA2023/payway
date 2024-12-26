package com.payway.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

@Data
@SuperBuilder
public class BankResponse {

    private boolean isSuccess;
    private String code;
    private String message;
    @JsonIgnore
    private HttpStatus httpStatus;

    private Object data;

    public BankResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public BankResponse(boolean isSuccess, String code, String message, HttpStatus httpStatus, Object data) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public BankResponse(boolean isSuccess, String message, HttpStatus httpStatus) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public BankResponse(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public BankResponse() {
    }
}
