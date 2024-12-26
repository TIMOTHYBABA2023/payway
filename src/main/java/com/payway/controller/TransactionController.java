package com.payway.controller;

import com.payway.dto.BankResponse;
import com.payway.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

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

    @GetMapping("/allTransaction")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<BankResponse> findAllTransaction(){
        BankResponse response = transactionService.findAllTransaction();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    @GetMapping("/{id}")
    public ResponseEntity<BankResponse> findById(@PathVariable Long id){
        BankResponse genericResponse = transactionService.getTransactionById(id);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }
    @GetMapping("/transactionsByUserId/{userId}")
    public ResponseEntity<BankResponse> findAllUserTransactionByUserId(@PathVariable Long userId){
        BankResponse genericResponse = transactionService.findAllUserTransactionByUserId(userId);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }
    @GetMapping("/user/{userId}/by-date")
    public ResponseEntity<BankResponse> getTransactionsByDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate){
        BankResponse genericResponse = transactionService.getTransactionsByDate(userId, startDate, endDate);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }

    @GetMapping("/user/{userId}/successful")
    public ResponseEntity<BankResponse> getSuccessfulTransactions(@PathVariable Long userId){
        BankResponse genericResponse = transactionService.getSuccessfulTransactions(userId);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }

    @GetMapping("/balanceEnquiry")
    public ResponseEntity<BankResponse> balanceEnquiry() {
        BankResponse bankResponse = transactionService.balanceEnquiry();
        return new ResponseEntity<BankResponse>(bankResponse, bankResponse.getHttpStatus());
    }
}