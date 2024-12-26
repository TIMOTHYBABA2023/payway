package com.payway.service.serviceImpl;

import com.payway.dto.BankResponse;
import com.payway.enums.AccountStatus;
import com.payway.enums.AccountTier;
import com.payway.enums.TransactionStatus;
import com.payway.enums.TransactionType;
import com.payway.exception.InvalidCredentialsException;
import com.payway.exception.ResourceNotFoundException;
import com.payway.model.Transaction;
import com.payway.model.User;
import com.payway.model.Wallet;
import com.payway.repository.TransactionRepository;
import com.payway.repository.UserRepository;
import com.payway.repository.WalletRepository;
import com.payway.service.TransactionService;
import com.payway.utils.AuthUserDetails;
import com.payway.utils.constants.CodeConstants;
import com.payway.utils.constants.WalletConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${app.universalAccount}")
    private String universalAccount;
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public BankResponse fundWallet(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(50)) < 0) {
            throw new InvalidCredentialsException("Funding amount must be greater than 50 Naira.");
        }
        Wallet wallet = findUserWallet();
        BigDecimal previousAccountBalance = wallet.getAccountBalance();
        BigDecimal newAccountBalance = previousAccountBalance.add(amount);

        wallet.setAccountBalance(newAccountBalance);

        if (newAccountBalance.compareTo(BigDecimal.valueOf(500)) >= 0) {
            wallet.setStatus(AccountStatus.ACTIVE);
        }

        walletRepository.save(wallet);

        return BankResponse.builder()
                .isSuccess(true)
                .code(CodeConstants.SUCCESS)
                .message("Wallet funded successfully. New balance: ₦" + wallet.getAccountBalance())
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public BankResponse withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(50)) < 0) {
            throw new InvalidCredentialsException("Withdrawal amount must be greater than 50 Naira.");
        }
        Wallet wallet = findUserWallet();
        BigDecimal previousAccountBalance = wallet.getAccountBalance();
        BigDecimal transactionCharge = calculateTransactionCharge(amount, wallet.getAccountTier());
        BigDecimal totalDeduction = amount.add(transactionCharge);

        if (previousAccountBalance.compareTo(totalDeduction) < 0) {
            throw new InvalidCredentialsException("Insufficient balance for requested amount.");
        }
        if(wallet.getStatus().equals(AccountStatus.INACTIVE)){
            throw new InvalidCredentialsException("Activate your wallet by funding with at least ₦500");
        }

        BigDecimal newAccountBalance = previousAccountBalance.subtract(totalDeduction);
        wallet.setAccountBalance(newAccountBalance);
        walletRepository.save(wallet);

        return BankResponse.builder()
                .isSuccess(true)
                .code(CodeConstants.SUCCESS)
                .message("Withdrawal of ₦" + amount + " was successful. New balance: ₦" + newAccountBalance)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    @Transactional
    public BankResponse transfer(BigDecimal amount, String recipientAccount) {
        Wallet senderWallet = findUserWallet();

        if (senderWallet.getStatus().equals(AccountStatus.INACTIVE)) {
            throw new InvalidCredentialsException("Activate your wallet by funding with at least ₦500");
        }
        BigDecimal previousAccountBalance = senderWallet.getAccountBalance();
        BigDecimal transactionCharge = calculateTransactionCharge(amount, senderWallet.getAccountTier());
        BigDecimal totalDeduction = amount.add(transactionCharge);

        if (previousAccountBalance.compareTo(totalDeduction) < 0) {
            throw new InvalidCredentialsException("Insufficient funds to cover transaction charges. Current balance: ₦" + previousAccountBalance);
        }
        logger.info("Account balance: {}", senderWallet.getAccountBalance());

        // Deduct total amount from sender's wallet
        BigDecimal newSenderBalance = senderWallet.getAccountBalance().subtract(totalDeduction);
        senderWallet.setAccountBalance(newSenderBalance);
        walletRepository.save(senderWallet);

        logger.info("Account balance: {}", senderWallet.getAccountBalance());

        // Log the calculated values for debugging
        logger.info("Amount: {}", amount);
        logger.info("Transaction Charge: {}", transactionCharge);
        logger.info("Total Deduction: {}", totalDeduction);

        // Add transaction charge to universal account
        Wallet universalWallet = walletRepository.findWalletByAccountNumber(universalAccount);
        BigDecimal newUniversalBalance = universalWallet.getAccountBalance().add(transactionCharge);
        universalWallet.setAccountBalance(newUniversalBalance);
        walletRepository.save(universalWallet);

        // Transfer amount to recipient's wallet
        Wallet recipientWallet = walletRepository.findWalletByAccountNumber(recipientAccount);

        if (recipientWallet.getStatus().equals(AccountStatus.INACTIVE)) {
            throw new InvalidCredentialsException("Recipient account is not active");
        }
        BigDecimal newRecipientBalance = recipientWallet.getAccountBalance().add(amount);
        recipientWallet.setAccountBalance(newRecipientBalance);
        walletRepository.save(recipientWallet);

        logTransaction(senderWallet.getId(), amount, transactionCharge, totalDeduction, recipientAccount, TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL);
        logger.info("Account balance: {}", senderWallet.getAccountBalance());

        return BankResponse.builder()
                .isSuccess(true)
                .message("Transfer successful. New balance: ₦" + senderWallet.getAccountBalance())
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private BigDecimal calculateTransactionCharge(BigDecimal amount, AccountTier accountTier) {
        BigDecimal rate = switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(0.012);
            case GOLD -> BigDecimal.valueOf(0.014);
            case PLATINUM -> BigDecimal.valueOf(0.015);
            default -> BigDecimal.valueOf(0.01);
        };
        return amount.multiply(rate);
    }
    private BigDecimal calculateTransactionLimit(BigDecimal amount, AccountTier accountTier) {
        return switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(500000);
            case GOLD -> BigDecimal.valueOf(1000000);
            case PLATINUM -> BigDecimal.valueOf(Double.MAX_VALUE);
            default -> BigDecimal.valueOf(200000);
        };
    }

    private void logTransaction(Long walletId, BigDecimal amount, BigDecimal charges, BigDecimal totalPaid, String recipient, TransactionType type, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setTransactionDate(new Date());
        transaction.setTransactionRecipient(recipient);
        transaction.setTransactionId(generateUniqueElevenDigits());
        transaction.setAmount(amount);
        transaction.setCharges(charges);
        transaction.setTotalPaid(totalPaid);
        transaction.setTransactionType(type);
        transaction.setTransactionStatus(status);
        transaction.setWalletId(walletId);
        transactionRepository.save(transaction);
    }

    private String generateUniqueElevenDigits() {
        String transactionId;
        do {
            transactionId = WalletConstants.generateAccountNumber();
        } while (transactionRepository.existsByTransactionId(transactionId));
        return transactionId;
    }


    @Override
    public BankResponse balanceEnquiry() {
        Wallet wallet = findUserWallet();
        BigDecimal accountBalance = wallet.getAccountBalance();

        return BankResponse.builder()
                .isSuccess(true)
                .code(CodeConstants.SUCCESS)
                .message(CodeConstants.SUCCESS_MESSAGE)
                .data(accountBalance.doubleValue())
                .httpStatus(HttpStatus.OK)
                .build();
    }


    private Wallet findUserWallet() {
        Long userId = AuthUserDetails.getAuthenticatedUserId();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("No user found with ID: {}", userId);
            throw new InvalidCredentialsException(CodeConstants.INVALID_CREDENTIALS_MESSAGE);
        }

        Wallet wallet = walletRepository.findWalletByUserId(user.getId());
        if (wallet == null) {
            logger.warn("No wallet found for user ID: {}", user.getId());
            throw new ResourceNotFoundException(CodeConstants.RESOURCE_NOT_FOUND_MESSAGE);
        }

        logger.info("Wallet found for user ID: {} with wallet ID: {}", user.getId(), wallet.getId());
        return wallet;
    }


}
