package com.payway.service.serviceImpl;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.enums.ERole;
import com.payway.model.User;
import com.payway.repository.UserRepository;
import com.payway.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public BankResponse getAllAdmin() {
        return null;
    }

    @Override
    public BankResponse getAdminById(Long id) {
        return null;
    }

    @Override
    public BankResponse createAdmin(User user) {
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
