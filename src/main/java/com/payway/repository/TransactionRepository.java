package com.payway.repository;

import com.payway.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Boolean existsByTransactionId(String transactionId);
}
