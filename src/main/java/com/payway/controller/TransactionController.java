package com.payway.controller;

import com.payway.dto.BankResponse;
import com.payway.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/fundWallet")
    public ResponseEntity<BankResponse> fundWallet(BigDecimal amount) {
        BankResponse bankResponse = transactionService.fundWallet(amount);
        return new ResponseEntity<BankResponse>(bankResponse, bankResponse.getHttpStatus());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BankResponse> withdrawal(BigDecimal amount) {
        BankResponse bankResponse = transactionService.withdraw(amount);
        return new ResponseEntity<BankResponse>(bankResponse, bankResponse.getHttpStatus());
    }

    @PostMapping("/transfer")
    public ResponseEntity<BankResponse> transferFunds(@RequestParam BigDecimal amount, @RequestParam String accountNumber) {
        BankResponse bankResponse = transactionService.transfer(amount, accountNumber);
        return new ResponseEntity<>(bankResponse, bankResponse.getHttpStatus());
    }

    @GetMapping("/balanceEnquiry")
    public ResponseEntity<BankResponse> balanceEnquiry() {
        BankResponse bankResponse = transactionService.balanceEnquiry();
        return new ResponseEntity<BankResponse>(bankResponse, bankResponse.getHttpStatus());
    }
}