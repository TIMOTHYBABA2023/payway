package com.payway.dto.responseDto;

import com.payway.enums.ERole;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SignupResponseDto {
    private Long id;
    private String email;
    private boolean enabled;
    private ERole role;

}
