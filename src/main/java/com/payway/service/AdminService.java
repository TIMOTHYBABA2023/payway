package com.payway.service;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.model.User;
import org.springframework.stereotype.Service;

public interface AdminService {
    BankResponse getAllAdmin();

    BankResponse getAdminById(Long id);

    BankResponse createAdmin(User user);

    BankResponse updateAdmin(UserUpdateDto userUpdateDto, Long id);

    BankResponse deleteAdminById(Long id);
}
