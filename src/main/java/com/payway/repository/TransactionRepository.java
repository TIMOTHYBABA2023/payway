package com.payway.repository;

import com.payway.enums.TransactionStatus;
import com.payway.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Boolean existsByTransactionId(String transactionId);
    List<Transaction> findByWalletIdAndTransactionDateBetween(Long walletId, Date startDate, Date endDate);

    List<Transaction> findAllByWalletIdAndTransactionStatus(Long walletId, TransactionStatus status);

}
