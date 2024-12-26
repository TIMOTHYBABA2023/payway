package com.payway.service.serviceImpl;

import com.payway.utils.constants.CodeConstants;
import com.payway.utils.constants.WalletConstants;
import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.SignUpRequestDto;
import com.payway.dto.requestDto.LoginRequestDto;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.dto.responseDto.LoginResponseDto;
import com.payway.dto.responseDto.SignupResponseDto;
import com.payway.dto.responseDto.UserResponseDto;
import com.payway.exception.RoleNotFoundException;
import com.payway.exception.UserAlreadyExistsException;
import com.payway.factory.RoleFactory;
import com.payway.model.User;
import com.payway.model.Wallet;
import com.payway.repository.RoleRepository;
import com.payway.repository.UserRepository;
import com.payway.repository.WalletRepository;
import com.payway.security.UserDetailsImpl;
import com.payway.security.jwt.JwtUtils;
import com.payway.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @Transactional
    public BankResponse signUpUser(SignUpRequestDto signUpRequestDto) {
        try {
            if (userRepository.existsByBvn(signUpRequestDto.getBvn())) {
                throw new UserAlreadyExistsException(CodeConstants.USER_ALREADY_EXISTS_MESSAGE + " Try signing in or provide another BVN.");
            }

            User user = createUser(signUpRequestDto);
            userRepository.save(user);

            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            wallet.setAccountNumber(generateUniqueAccountNumber());
            walletRepository.save(wallet);

            SignupResponseDto signupResponseDto = new SignupResponseDto(
                    user.getId(),
                    user.getEmail(),
                    user.isEnabled(),
                    user.getRole()
            );

            return BankResponse.builder()
                    .isSuccess(true)
                    .code(CodeConstants.CREATED)
                    .message(CodeConstants.CREATED_MESSAGE)
                    .httpStatus(HttpStatus.CREATED)
                    .data(signupResponseDto)
                    .build();
        } catch (Exception e) {
            return handleException(e);
        }
    }


    @Override
    public BankResponse signInUser(LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .username(userDetails.getUsername())
                    .id(userDetails.getId())
                    .token(jwt)
                    .type("Bearer")
                    .roles(roles)
                    .build();

            return BankResponse.builder()
                    .isSuccess(true)
                    .code(CodeConstants.SUCCESS)
                    .message("Login " + CodeConstants.SUCCESS_MESSAGE)
                    .data(loginResponseDto)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    public BankResponse getAllUsers() {
        List<User> userList = userRepository.findAll();

        return BankResponse.builder()
                .isSuccess(true)
                .code(CodeConstants.FOUND)
                .data(userList)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }

    @Override
    public BankResponse getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()){
            return BankResponse.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .isSuccess(false)
                    .message("Provide valid user Id!").build();
        }
        UserResponseDto userResponseDto = new UserResponseDto();
        BeanUtils.copyProperties(user.get(), userResponseDto);

        return BankResponse.builder()
                .httpStatus(HttpStatus.FOUND)
                .code(CodeConstants.FOUND)
                .isSuccess(true)
                .data(userResponseDto).build();
    }

    @Override
    public BankResponse updateUser(UserUpdateDto userUpdateDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        userRepository.save(user);
        return BankResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public BankResponse deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        userRepository.delete(user);
        return BankResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private User createUser(SignUpRequestDto signUpRequestDto) throws RoleNotFoundException {
        String userName;
        if (signUpRequestDto.getMiddleName() == null) {
            userName = signUpRequestDto.getFirstname() + " " + signUpRequestDto.getLastname();
        } else userName = signUpRequestDto.getFirstname() + " " + signUpRequestDto.getLastname()+ " " +signUpRequestDto.getMiddleName();
        return User.builder()
                .email(signUpRequestDto.getEmail())
                .lastname(signUpRequestDto.getLastname())
                .firstname(signUpRequestDto.getFirstname())
                .middleName(signUpRequestDto.getMiddleName())
                .bvn(signUpRequestDto.getBvn())
                .username(userName)
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .enabled(true)
                .role(signUpRequestDto.getRole())
                .build();
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = WalletConstants.generateAccountNumber();
        } while (walletRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private BankResponse handleException(Throwable ex) {
        ex.printStackTrace();
        if (ex instanceof UserAlreadyExistsException) {
            return BankResponse.builder()
                    .isSuccess(false)
                    .message(ex.getMessage())
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        } else if (ex instanceof RoleNotFoundException) {
            return BankResponse.builder()
                    .isSuccess(false)
                    .message("Registration Failed: Specified role not found.")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        } else {
            System.out.println("Unexpected error: " + ex.getMessage());
            return BankResponse.builder()
                    .isSuccess(false)
                    .message("An unexpected error occurred during registration.")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }


}
