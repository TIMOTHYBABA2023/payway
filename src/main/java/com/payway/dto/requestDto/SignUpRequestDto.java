package com.payway.dto.requestDto;

import com.payway.enums.ERole;
import com.payway.utils.ValidBVN;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    private LocalDate dateOfBirth;
    private String address;

    @NotBlank(message = "Firstname is required!")
    @Size(min = 3, message = "Firstname must have at least 3 characters!")
    @Size(max = 20, message = "Firstname can have at most 20 characters!")
    private String firstname;

    @NotBlank(message = "Lastname is required!")
    @Size(min = 3, message = "Lastname must have at least 3 characters!")
    @Size(max = 20, message = "Lastname can have at most 20 characters!")
    private String lastname;

    @Size(min = 3, message = "Lastname must have at least 3 characters!")
    @Size(max = 20, message = "Lastname can have at most 20 characters!")
    private String middleName;

    @Email(message = "Email is not in valid format!")
    @NotBlank(message = "Email is required!")
    private String email;

    @NotBlank(message = "Password is required!")
    @Size(min = 8, message = "Password must have at least 8 characters!")
    @Size(max = 20, message = "Password can have at most 20 characters!")
    private String password;

    @ValidBVN(message = "Invalid BVN format. Must be 11 digits")
    @NotBlank(message = "BVN is required")    private String bvn;

    private ERole role;


}