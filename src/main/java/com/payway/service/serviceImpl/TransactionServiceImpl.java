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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @Transactional
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
        fundingTransaction(wallet.getId(), amount, amount, wallet.getAccountBalance(), wallet.getAccountNumber(), TransactionStatus.SUCCESSFUL);

        return BankResponse.builder()
                .isSuccess(true)
                .code(CodeConstants.SUCCESS)
                .message("Wallet funded successfully. New balance: ₦" + wallet.getAccountBalance())
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    @Transactional
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
        if (wallet.getStatus().equals(AccountStatus.INACTIVE)) {
            throw new InvalidCredentialsException("Activate your wallet by funding with at least ₦500");
        }

        BigDecimal newAccountBalance = previousAccountBalance.subtract(totalDeduction);
        wallet.setAccountBalance(newAccountBalance);
        walletRepository.save(wallet);

        // Add transaction charge to universal account
        Wallet universalWallet = walletRepository.findWalletByAccountNumber(universalAccount);
        BigDecimal newUniversalBalance = universalWallet.getAccountBalance().add(transactionCharge);
        universalWallet.setAccountBalance(newUniversalBalance);
        walletRepository.save(universalWallet);
        fundingTransaction(universalWallet.getId(), transactionCharge, transactionCharge, universalWallet.getAccountBalance(), universalAccount, TransactionStatus.SUCCESSFUL);


        withdrawalTransaction(wallet.getId(), amount, transactionCharge, totalDeduction, wallet.getAccountBalance(), wallet.getAccountNumber(), TransactionStatus.SUCCESSFUL);

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

        BigDecimal transactionLimit = calculateInstantTransactionLimit(totalDeduction, senderWallet.getAccountTier());
        if (totalDeduction.compareTo(transactionLimit) > 0) {
            throw new InvalidCredentialsException("Transaction amount exceeds the limit for your account tier.");
        }

        // Check daily and weekly transaction limits
        checkDailyTransactionLimit(senderWallet, totalDeduction);
        checkWeeklyTransactionLimit(senderWallet, totalDeduction);

        // Log the calculated values for debugging
        logger.info("Amount: {}", amount);
        logger.info("Transaction Charge: {}", transactionCharge);
        logger.info("Total Deduction: {}", totalDeduction);
        logger.info("Previous Account Balance: {}", previousAccountBalance);

        // Deduct total amount from sender's wallet
        deductBalance(senderWallet, totalDeduction);

        // Add transaction charge to universal account
        Wallet universalWallet = walletRepository.findWalletByAccountNumber(universalAccount);
        BigDecimal newUniversalBalance = universalWallet.getAccountBalance().add(transactionCharge);
        universalWallet.setAccountBalance(newUniversalBalance);
        walletRepository.save(universalWallet);
        fundingTransaction(universalWallet.getId(), transactionCharge, transactionCharge, universalWallet.getAccountBalance(), universalAccount, TransactionStatus.SUCCESSFUL);

        // Log the new universal balance
        logger.info("New Universal Balance: {}", newUniversalBalance);

        // Recipient's wallet
        Wallet recipientWallet = walletRepository.findWalletByAccountNumber(recipientAccount);

        // Check maximum balance limit for recipient
        BigDecimal recipientMaxBalance = getMaxBalanceLimit(recipientWallet.getAccountTier());
        BigDecimal newRecipientBalance = recipientWallet.getAccountBalance().add(amount);

        if (newRecipientBalance.compareTo(recipientMaxBalance) > 0) {
            throw new InvalidCredentialsException("Recipient's account balance exceeds the maximum limit for their account tier.");
        }

        // Update recipient's balance
        recipientWallet.setAccountBalance(newRecipientBalance);
        walletRepository.save(recipientWallet);
        fundingTransaction(recipientWallet.getId(), amount, amount, recipientWallet.getAccountBalance(), recipientAccount, TransactionStatus.SUCCESSFUL);

        // Log the new recipient balance
        logger.info("New Recipient Balance: {}", newRecipientBalance);

        return BankResponse.builder()
                .isSuccess(true)
                .message("Transfer successful. New balance: ₦" + senderWallet.getAccountBalance())
                .httpStatus(HttpStatus.OK)
                .build();
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

    @Override
    public BankResponse findAllTransaction() {
        List<Transaction> transactions = transactionRepository.findAll();
        return BankResponse.builder()
                .code(CodeConstants.FOUND)
                .message(CodeConstants.FOUND_MESSAGE)
                .data(transactions)
                .isSuccess(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }

    @Override
    public BankResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElse(null);
        if (transaction == null){
            throw new ResourceNotFoundException("Transaction not found with provided transaction Id!");
        }
        return BankResponse.builder()
                .httpStatus(HttpStatus.FOUND)
                .code(CodeConstants.FOUND)
                .message(CodeConstants.FOUND_MESSAGE)
                .data(transaction)
                .isSuccess(true)
                .build();
    }

    @Override
    public BankResponse findAllUserTransactionByUserId(Long userId) {
        Wallet wallet = walletRepository.findWalletByUserId(userId);
        List<Transaction> transactions = transactionRepository.findAll();
        List<Transaction> transactionsByUserId = new ArrayList<>();
        if (transactions.isEmpty()){
            throw new ResourceNotFoundException("Transaction list is empty");
        }
        for (Transaction transaction: transactions){
            if (transaction.getWalletId().equals(wallet.getUserId())){
                transactionsByUserId.add(transaction);
            }
        }

        return BankResponse.builder()
                .code(CodeConstants.FOUND)
                .message(CodeConstants.FOUND_MESSAGE)
                .data(transactionsByUserId)
                .isSuccess(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }

    @Override
    public BankResponse getTransactionsByDate(Long userId, Date startDate, Date endDate) {
        Wallet wallet = walletRepository.findWalletByUserId(userId);
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user ID: " + userId);
        }
        List<Transaction> transactions = transactionRepository.findByWalletIdAndTransactionDateBetween(wallet.getId(), startDate, endDate);

        return BankResponse.builder()
                .code(CodeConstants.FOUND)
                .message(CodeConstants.FOUND_MESSAGE)
                .data(transactions)
                .isSuccess(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }


    @Override
    public BankResponse getSuccessfulTransactions(Long userId) {
        Wallet wallet = walletRepository.findWalletByUserId(userId);
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user ID: " + userId);
        }
        List<Transaction> successfulTransactions = transactionRepository.findAllByWalletIdAndTransactionStatus(wallet.getId(), TransactionStatus.SUCCESSFUL);

        return BankResponse.builder()
                .code(CodeConstants.FOUND)
                .message(CodeConstants.FOUND_MESSAGE)
                .data(successfulTransactions)
                .isSuccess(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }


    // .................Reusable Methods...............

    private BigDecimal calculateTransactionCharge(BigDecimal amount, AccountTier accountTier) {
        BigDecimal rate = switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(0.012);
            case GOLD -> BigDecimal.valueOf(0.014);
            case PLATINUM -> BigDecimal.valueOf(0.015);
            default -> BigDecimal.valueOf(0.01);
        };
        return amount.multiply(rate);
    }

    private BigDecimal calculateInstantTransactionLimit(BigDecimal amount, AccountTier accountTier) {
        return switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(200000); // Example: 500,000
            case GOLD -> BigDecimal.valueOf(500000); // Example: 1,000,000
            case PLATINUM -> BigDecimal.valueOf(Double.MAX_VALUE); // Unlimited
            default -> BigDecimal.valueOf(100000); // Example: 200,000
        };
    }

    private BigDecimal getDailyTransactionLimit(AccountTier accountTier) {
        return switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(500000); // Example: 500,000
            case GOLD -> BigDecimal.valueOf(1000000); // Example: 1,000,000
            case PLATINUM -> BigDecimal.valueOf(Double.MAX_VALUE); // Unlimited
            default -> BigDecimal.valueOf(200000); // Example: 200,000
        };
    }

    private BigDecimal getWeeklyTransactionLimit(AccountTier accountTier) {
        return switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(2000000); // Example: 2,000,000
            case GOLD -> BigDecimal.valueOf(5000000); // Example: 5,000,000
            case PLATINUM -> BigDecimal.valueOf(Double.MAX_VALUE); // Unlimited
            default -> BigDecimal.valueOf(1000000); // Example: 1,000,000
        };
    }

    private void checkDailyTransactionLimit(Wallet senderWallet, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        Date startOfDay = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        BigDecimal dailyLimit = getDailyTransactionLimit(senderWallet.getAccountTier());
        BigDecimal totalDailyTransactions = getTotalTransactionsForPeriod(senderWallet.getId(), startOfDay, endOfDay);
        if (totalDailyTransactions.add(amount).compareTo(dailyLimit) > 0) {
            throw new InvalidCredentialsException("Daily transaction limit exceeded.");
        }
    }

    private void checkWeeklyTransactionLimit(Wallet senderWallet, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Date startOfWeekDate = Date.from(startOfWeek.atZone(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        BigDecimal weeklyLimit = getWeeklyTransactionLimit(senderWallet.getAccountTier());
        BigDecimal totalWeeklyTransactions = getTotalTransactionsForPeriod(senderWallet.getId(), startOfWeekDate, endOfDay);
        if (totalWeeklyTransactions.add(amount).compareTo(weeklyLimit) > 0) {
            throw new InvalidCredentialsException("Weekly transaction limit exceeded.");
        }
    }

    private BigDecimal getTotalTransactionsForPeriod(Long walletId, Date start, Date end) {
        List<Transaction> transactions = transactionRepository.findByWalletIdAndTransactionDateBetween(walletId, start, end);
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getMaxBalanceLimit(AccountTier accountTier) {
        return switch (accountTier) {
            case SILVER -> BigDecimal.valueOf(1000000);
            case GOLD -> BigDecimal.valueOf(5000000);
            case PLATINUM -> BigDecimal.valueOf(Double.MAX_VALUE);
            default -> BigDecimal.valueOf(500000);
        };
    }

    private void deductBalance(Wallet wallet, BigDecimal totalDeduction) {
        BigDecimal previousAccountBalance = wallet.getAccountBalance();
        if (previousAccountBalance.compareTo(totalDeduction) < 0) {
            throw new InvalidCredentialsException("Insufficient funds to cover transaction charges. Current balance: ₦" + previousAccountBalance);
        }
        BigDecimal newBalance = previousAccountBalance.subtract(totalDeduction);
        wallet.setAccountBalance(newBalance);
        walletRepository.save(wallet);
        logger.info("New Balance for Wallet ID {}: {}", wallet.getId(), newBalance);
    }

    private void fundingTransaction(Long walletId, BigDecimal amount, BigDecimal totalReceived, BigDecimal accountBalance, String recipient, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setTransactionDate(new Date());
        transaction.setTransactionRecipient(recipient);
        transaction.setTransactionId(generateUniqueElevenDigits());
        transaction.setAmount(amount);
        transaction.setTotalReceived(totalReceived);
        transaction.setAccountBalance(accountBalance);
        transaction.setTransactionType(TransactionType.FUND);
        transaction.setTransactionStatus(status);
        transaction.setWalletId(walletId);
        transactionRepository.save(transaction);
    }

    private void withdrawalTransaction(Long walletId, BigDecimal amount, BigDecimal charges, BigDecimal totalPaid, BigDecimal accountBalance, String recipient, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setTransactionDate(new Date());
        transaction.setTransactionRecipient(recipient);
        transaction.setTransactionId(generateUniqueElevenDigits());
        transaction.setAmount(amount);
        transaction.setCharges(charges);
        transaction.setTotalPaid(totalPaid);
        transaction.setAccountBalance(accountBalance);
        transaction.setTransactionType(TransactionType.WITHDRAW);
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
