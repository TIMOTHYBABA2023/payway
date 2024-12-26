package com.payway.service;

import com.payway.dto.BankResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


public interface TransactionService {

    BankResponse fundWallet(BigDecimal amount);

    BankResponse withdraw(BigDecimal amount);

    BankResponse transfer(BigDecimal amount, String accountNumber);

    BankResponse balanceEnquiry();

    BankResponse findAllTransaction();

    BankResponse getTransactionById(Long id);

    BankResponse findAllUserTransactionByUserId(Long userId);

    BankResponse getTransactionsByDate(Long userId, Date startDate, Date endDate);

    BankResponse getSuccessfulTransactions(Long userId);
}
