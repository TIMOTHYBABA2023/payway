package com.payway.dto.responseDto;

import com.payway.model.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserResponseDto {
    private String firstname;
    private String lastname;
    private String middleName;
    private String username;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String phoneNumber;
}
