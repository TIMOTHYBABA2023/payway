package com.payway.exception;

import com.payway.dto.BankResponse;
import com.payway.utils.constants.CodeConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<BankResponse> handleInvalidCredentialsException(InvalidCredentialsException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        BankResponse.builder()
                                .isSuccess(false)
                                .code(CodeConstants.INVALID_CREDENTIALS)
                                .message(CodeConstants.INVALID_CREDENTIALS_MESSAGE)
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = UserAlreadyExistsException.class)
    public ResponseEntity<BankResponse> handleUserAlreadyExistsException(UserAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        BankResponse.builder()
                                .isSuccess(false)
                                .code(CodeConstants.USER_ALREADY_EXISTS)
                                .message(CodeConstants.USER_ALREADY_EXISTS_MESSAGE)
                                .httpStatus(HttpStatus.CONFLICT)
                                .build()
                );
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<BankResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        BankResponse.builder()
                                .isSuccess(false)
                                .code(CodeConstants.RESOURCE_NOT_FOUND)
                                .message(CodeConstants.RESOURCE_NOT_FOUND_MESSAGE)
                                .httpStatus(HttpStatus.NOT_FOUND)
                                .build()
                );
    }

    @ExceptionHandler(value = RoleNotFoundException.class)
    public ResponseEntity<BankResponse> handleRoleNotFoundException(RoleNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        BankResponse.builder()
                                .isSuccess(false)
                                .code(CodeConstants.RESOURCE_NOT_FOUND)
                                .httpStatus(HttpStatus.NOT_FOUND)
                                .build()
                );
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<BankResponse> handleGeneralException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        BankResponse.builder()
                                .isSuccess(false)
                                .code(CodeConstants.INTERNAL_SERVER_ERROR)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build()
                );
    }
}
