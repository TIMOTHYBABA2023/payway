package com.payway.service;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.SignUpRequestDto;
import com.payway.dto.requestDto.LoginRequestDto;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.model.User;
import org.springframework.stereotype.Service;


public interface UserService {
    BankResponse signUpUser(SignUpRequestDto signUpRequestDto);
    BankResponse signInUser(LoginRequestDto loginRequestDto);

    BankResponse getAllUsers();

    BankResponse getUserById(Long id);

    BankResponse updateUser(UserUpdateDto userUpdateDto, Long id);

    BankResponse deleteUser(Long userId);

    BankResponse upgradeToSilverTier(String silverTierRequest);

    BankResponse upgradeToGoldTier(String goldTierRequest);

    BankResponse upgradeToPlatinumTier(String platinumTierRequest);
}
