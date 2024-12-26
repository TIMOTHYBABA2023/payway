package com.payway.repository;

import com.payway.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByAccountNumber(String accountNumber);
    Wallet findWalletByUserId(Long userId);
    Wallet findWalletByAccountNumber(String accountNumber);
}
