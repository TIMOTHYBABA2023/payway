package com.payway.controller;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.LoginRequestDto;
import com.payway.dto.requestDto.SignUpRequestDto;
import com.payway.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<BankResponse> registerUser(@RequestBody @Valid SignUpRequestDto signUpRequestDto) {
        BankResponse bankResponse = userService.signUpUser(signUpRequestDto);
        return new ResponseEntity<>(bankResponse, bankResponse.getHttpStatus());
    }



    @PostMapping("/login")
    public ResponseEntity<BankResponse> signInUser(@RequestBody @Valid LoginRequestDto loginRequestDto){
        BankResponse bankResponse = userService.signInUser(loginRequestDto);
        return new ResponseEntity<>(bankResponse, bankResponse.getHttpStatus());
    }
}
