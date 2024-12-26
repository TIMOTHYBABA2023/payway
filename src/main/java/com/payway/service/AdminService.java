package com.payway.service;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.UserUpdateDto;
import org.springframework.stereotype.Service;

public interface AdminService {
    BankResponse getAllAdmin();

    BankResponse getAdminById(Long id);

    BankResponse createAdmin(UserUpdateDto userUpdateDto, Long id);

    BankResponse updateAdmin(UserUpdateDto userUpdateDto, Long id);

    BankResponse deleteAdminById(Long id);
}
