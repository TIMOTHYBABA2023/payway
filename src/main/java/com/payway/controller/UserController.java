package com.payway.controller;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.service.UserService;
import com.payway.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
    }

    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<BankResponse> findAllUser(){
        BankResponse response = userService.getAllUsers();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<BankResponse> findById(@PathVariable Long id){
        BankResponse genericResponse = userService.getUserById(id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }


    @PatchMapping("/{id}")
    public ResponseEntity<BankResponse> updateUser(@RequestBody UserUpdateDto userUpdateDto, @PathVariable Long id){
        BankResponse genericResponse = userService.updateUser(userUpdateDto, id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BankResponse> deleteUser(@PathVariable Long userId){

        BankResponse genericResponse = userService.deleteUser(userId);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }
}