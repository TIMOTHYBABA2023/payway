package com.payway.controller;

import com.payway.dto.BankResponse;
import com.payway.dto.requestDto.UserUpdateDto;
import com.payway.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin")
public class AdminController {


    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/allAdmins")
    public ResponseEntity<BankResponse> findAllAdmin(){
        BankResponse response = adminService.getAllAdmin();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankResponse> findById(@PathVariable Long id){
        BankResponse genericResponse = adminService.getAdminById(id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }


    @PostMapping("")
    public ResponseEntity<BankResponse> createAdmin(@RequestBody UserUpdateDto userUpdateDto, @PathVariable Long id){
        BankResponse genericResponse = adminService.createAdmin(userUpdateDto, id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BankResponse> updateAdmin(@RequestBody UserUpdateDto userUpdateDto, @PathVariable Long id){
        BankResponse genericResponse = adminService.updateAdmin(userUpdateDto, id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BankResponse> deleteAdmin(@PathVariable Long id){

        BankResponse genericResponse = adminService.deleteAdminById(id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }
}
