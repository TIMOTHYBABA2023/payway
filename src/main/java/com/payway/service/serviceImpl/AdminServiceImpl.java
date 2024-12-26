package com.payway.service.serviceImpl;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Override
    public BankResponse getAllAdmin() {
        return null;
    }

    @Override
    public BankResponse getAdminById(Long id) {
        return null;
    }

    @Override
    public BankResponse createAdmin(UserUpdateDto userUpdateDto, Long id) {
        return null;
    }

    @Override
    public BankResponse updateAdmin(UserUpdateDto userUpdateDto, Long id) {
        return null;
    }

    @Override
    public BankResponse deleteAdminById(Long id) {
        return null;
    }
}
