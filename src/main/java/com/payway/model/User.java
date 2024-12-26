package com.payway.model;

import com.payway.enums.ERole;
import com.payway.model.commonentities.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends Auditable {

    private String firstname;
    private String lastname;
    private String middleName;
    private String username;
    private boolean enabled;
    private String password;
    private LocalDate dateOfBirth;
    private String address;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, length = 11, nullable = false)
    private String bvn;

    @Enumerated(EnumType.STRING)
    private ERole role;

    private String phoneNumber;
    private String NIN;
    private String utilityBill;

}
