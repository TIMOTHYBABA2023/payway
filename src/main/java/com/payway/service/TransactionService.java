package com.payway.service;

import com.payway.dto.BankResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


public interface TransactionService {

    BankResponse fundWallet(BigDecimal amount);

    BankResponse withdraw(BigDecimal amount);

    BankResponse transfer(BigDecimal amount, String accountNumber);

    BankResponse balanceEnquiry();
}
