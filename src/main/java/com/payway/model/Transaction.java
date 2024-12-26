package com.payway.model;

import com.payway.enums.TransactionStatus;
import com.payway.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date transactionDate;
    private String transactionId;
    private String transactionRecipient;
    private BigDecimal amount;
    private BigDecimal charges;
    private BigDecimal totalPaid;
    private BigDecimal totalReceived;
    private BigDecimal accountBalance;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    private Long walletId;

}
