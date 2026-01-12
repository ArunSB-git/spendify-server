package com.app.money_tracker_backend.service;

import com.app.money_tracker_backend.config.SecurityUtil;
import com.app.money_tracker_backend.dto.AddAmountRequest;
import com.app.money_tracker_backend.dto.TransactionLogResponse;
import com.app.money_tracker_backend.dto.TransactionRequest;
import com.app.money_tracker_backend.dto.TransactionResponse;
import com.app.money_tracker_backend.model.Bank;
import com.app.money_tracker_backend.model.Transaction;
import com.app.money_tracker_backend.model.TransactionLog;
import com.app.money_tracker_backend.model.User;
import com.app.money_tracker_backend.repository.BankRepository;
import com.app.money_tracker_backend.repository.TransactionLogRepository;
import com.app.money_tracker_backend.repository.TransactionRepository;
import com.app.money_tracker_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;

    // 🔹 Get the currently logged-in user
    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔹 Helper to map Transaction to TransactionResponse
    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionName(),
                tx.getTransactionType(),
                tx.getAmount(),
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                tx.getBank() != null ? tx.getBank().getId() : null
        );
    }

    private TransactionLogResponse toResponse(TransactionLog log) {
        return new TransactionLogResponse(
                log.getId(), // ✅ log primary key
                log.getTransactionName(),
                log.getTransactionType(),
                log.getAmount(),
                log.getCreatedAt(),
                log.getBank() != null ? log.getBank().getId() : null, // ✅ bankId
                log.getAction() // ✅ action
        );
    }




    // 🔹 Create a transaction
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getCurrentUser();

        Bank bank = null;
        if (request.getBankId() != null) {
            bank = bankRepository.findById(request.getBankId())
                    .orElseThrow(() -> new RuntimeException("Bank not found"));
        }

        Transaction tx = Transaction.builder()
                .transactionName(request.getTransactionName())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .user(user)
                .bank(bank) // Set bank
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        Transaction savedTx = transactionRepository.save(tx);

        // 🔹 Log transaction creation
        TransactionLog log = TransactionLog.builder()
                .transactionId(tx.getId())
                .user(user)
                .bank(bank)
                .transactionName(savedTx.getTransactionName())
                .transactionType(savedTx.getTransactionType())
                .amount(savedTx.getAmount())
                .action("Created a new transaction")
                .createdAt(LocalDateTime.now())
                .build();
        transactionLogRepository.save(log);

        return toResponse(savedTx);
    }

    // 🔹 Update a transaction
    @Transactional
    public TransactionResponse updateTransaction(String transactionId, TransactionRequest request) {
        User user = getCurrentUser();

        Transaction tx = transactionRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        Bank bank = null;
        if (request.getBankId() != null) {
            bank = bankRepository.findById(request.getBankId())
                    .orElseThrow(() -> new RuntimeException("Bank not found"));
        }

        tx.setTransactionName(request.getTransactionName());
        tx.setTransactionType(request.getTransactionType());
        tx.setAmount(request.getAmount());
        tx.setBank(bank);
        tx.setUpdatedAt(LocalDateTime.now());

        Transaction updatedTx = transactionRepository.save(tx);

        // 🔹 Log transaction update
        transactionLogRepository.save(
                TransactionLog.builder()
                        .transactionId(tx.getId())
                        .user(user)
                        .bank(bank)
                        .transactionName(updatedTx.getTransactionName())
                        .transactionType(updatedTx.getTransactionType())
                        .amount(updatedTx.getAmount())
                        .action("Amount for this transaction has been updated")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return toResponse(updatedTx);
    }

    // 🔹 Get all transactions for current user
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findAllByUserIdAndDeletedFalseOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<TransactionLogResponse> getAllTransactionsLogs() {
        User user = getCurrentUser();
        return transactionLogRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse addAmountToTransaction(AddAmountRequest request) {

        User user = getCurrentUser();

        Transaction tx = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 🔐 Ownership check
        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (request.getAmountToAdd() == null ||
                request.getAmountToAdd().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount to add must be greater than zero");
        }

        // 🔹 Add amount
        BigDecimal updatedAmount = tx.getAmount().add(request.getAmountToAdd());
        tx.setAmount(updatedAmount);
        tx.setUpdatedAt(LocalDateTime.now());

        Transaction updatedTx = transactionRepository.save(tx);

        // 🔹 Log ADD action
        TransactionLog log = TransactionLog.builder()
                .transactionId(tx.getId())
                .user(user)
                .bank(tx.getBank())
                .transactionName(tx.getTransactionName())
                .transactionType(tx.getTransactionType())
                .amount(request.getAmountToAdd()) // log only added amount
                .action("Added money to this existing transaction")
                .createdAt(LocalDateTime.now())
                .build();

        transactionLogRepository.save(log);

        return toResponse(updatedTx);
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {

        User user = getCurrentUser();

        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 🔐 Ownership check
        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // 📝 Log delete action (AUDIT SAFE)
        TransactionLog deleteLog = TransactionLog.builder()
                .transactionId(tx.getId())
                .user(user)
                .bank(tx.getBank())
                .transactionName(tx.getTransactionName())
                .transactionType(tx.getTransactionType())
                .amount(tx.getAmount())
                .action("Transaction was deleted")
                .createdAt(LocalDateTime.now())
                .build();

        transactionLogRepository.save(deleteLog);

        tx.setDeleted(true);       // Mark as deleted
        tx.setUpdatedAt(LocalDateTime.now()); // Optional: update timestamp
        transactionRepository.save(tx);
    }





}
