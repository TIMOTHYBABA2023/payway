package com.payway.model;

import com.payway.enums.AccountStatus;
import com.payway.enums.AccountTier;
import com.payway.model.commonentities.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallets")
public class Wallet extends Auditable {
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @Column(unique = true, length = 10, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    private AccountTier accountTier = AccountTier.BASIC;

    @Column(nullable = false)
    private Long userId;
}