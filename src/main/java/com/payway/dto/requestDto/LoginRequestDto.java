package com.payway.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "Email is required!")
    private String email;

    @NotBlank(message = "Password is required!")
    private String password;

}
