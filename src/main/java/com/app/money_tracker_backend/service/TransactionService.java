package com.app.money_tracker_backend.service;

import com.app.money_tracker_backend.config.SecurityUtil;
import com.app.money_tracker_backend.dto.*;
import com.app.money_tracker_backend.enums.TransactionType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                .transactionType(TransactionType.valueOf(request.getTransactionType()))
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
        tx.setTransactionType(TransactionType.valueOf(request.getTransactionType()));
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

    @Transactional(readOnly = true)
    public List<MonthlyTransactionSpentResponse> calculateSpent(
            String type,
            Integer year,
            Integer month
    ) {
        User user = getCurrentUser();

        LocalDateTime start;
        LocalDateTime end;

        // Determine the date range
        switch (type.toUpperCase()) {
            case "TODAY" -> {
                start = LocalDate.now().atStartOfDay();
                end = LocalDate.now().atTime(LocalTime.MAX);
            }
            case "MONTH" -> {
                if (year == null || month == null)
                    throw new IllegalArgumentException("Year and month are required for MONTH");
                start = LocalDate.of(year, month, 1).atStartOfDay();
                end = start.plusMonths(1).minusSeconds(1);
            }
            case "YEAR" -> {
                if (year == null)
                    throw new IllegalArgumentException("Year is required for YEAR");
                start = LocalDate.of(year, 1, 1).atStartOfDay();
                end = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
            }
            default -> throw new IllegalArgumentException("Invalid type: TODAY, MONTH, YEAR");
        }

        // 1️⃣ Get all current DEBIT transactions for the user
        List<Transaction> debitTransactions = transactionRepository
                .findByUserIdAndTransactionTypeAndDeletedFalse(user.getId(), TransactionType.DEBIT);

        if (debitTransactions.isEmpty()) {
            return Collections.emptyList();
        }

        // 2️⃣ Get all transaction logs for these DEBIT transactions in the date range
        List<UUID> debitTransactionIds = debitTransactions.stream()
                .map(Transaction::getId)
                .toList();

        List<TransactionLog> logs = transactionLogRepository
                .findByTransactionIdInAndCreatedAtBetweenOrderByCreatedAtAsc(
                        debitTransactionIds, start, end
                );

        // 3️⃣ Calculate cumulative amount per transaction
        Map<UUID, BigDecimal> totals = new HashMap<>();
        Map<UUID, String> names = new HashMap<>();
        Set<UUID> deleted = new HashSet<>();

        for (TransactionLog log : logs) {
            UUID txId = log.getTransactionId();

            if (deleted.contains(txId)) continue;

            names.putIfAbsent(txId, log.getTransactionName());

            switch (log.getAction()) {
                case "Created a new transaction" -> totals.put(txId, log.getAmount());
                case "Added money to this existing transaction" ->
                        totals.merge(txId, log.getAmount(), BigDecimal::add);
                case "Amount for this transaction has been updated" -> totals.put(txId, log.getAmount());
                case "Transaction was deleted" -> {
                    totals.remove(txId);
                    deleted.add(txId);
                }
            }
        }

        // 4️⃣ Build response
        return totals.entrySet()
                .stream()
                .map(e -> new MonthlyTransactionSpentResponse(
                        e.getKey(),
                        names.get(e.getKey()),
                        e.getValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTransactionSummaryResponse> getYearlyTransactionSummary(
            Integer year,
            TransactionType transactionType
    ) {

        if (year == null) {
            throw new IllegalArgumentException("Year is required");
        }

        User user = getCurrentUser();

        // 1️⃣ Get current transactions of requested type
        List<Transaction> transactions =
                transactionRepository.findByUserIdAndTransactionTypeAndDeletedFalse(
                        user.getId(),
                        transactionType
                );

        if (transactions.isEmpty()) {
            return IntStream.rangeClosed(1, 12)
                    .mapToObj(m -> new MonthlyTransactionSummaryResponse(m, List.of()))
                    .toList();
        }

        Map<UUID, String> transactionNames = transactions.stream()
                .collect(Collectors.toMap(Transaction::getId, Transaction::getTransactionName));

        List<UUID> transactionIds = transactions.stream()
                .map(Transaction::getId)
                .toList();

        List<MonthlyTransactionSummaryResponse> response = new ArrayList<>();

        // 2️⃣ Month loop
        for (int month = 1; month <= 12; month++) {

            LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);

            List<TransactionLog> logs =
                    transactionLogRepository.findByTransactionIdInAndCreatedAtBetweenOrderByCreatedAtAsc(
                            transactionIds,
                            start,
                            end
                    );

            Map<UUID, BigDecimal> totals = new HashMap<>();
            Set<UUID> deleted = new HashSet<>();

            for (TransactionLog log : logs) {

                UUID txId = log.getTransactionId();
                if (deleted.contains(txId)) continue;

                switch (log.getAction()) {

                    case "Created a new transaction" ->
                            totals.put(txId, log.getAmount());

                    case "Added money to this existing transaction" ->
                            totals.merge(txId, log.getAmount(), BigDecimal::add);

                    case "Amount for this transaction has been updated" ->
                            totals.put(txId, log.getAmount());

                    case "Transaction was deleted" -> {
                        totals.remove(txId);
                        deleted.add(txId);
                    }
                }
            }

            // 3️⃣ Convert to transaction list
            List<MonthlyTransactionRecordResponse> transactionResponses =
                    totals.entrySet().stream()
                            .map(e -> new MonthlyTransactionRecordResponse(
                                    e.getKey(),
                                    transactionNames.get(e.getKey()),
                                    e.getValue()
                            ))
                            .toList();

            response.add(new MonthlyTransactionSummaryResponse(month, transactionResponses));
        }

        return response;
    }





}
