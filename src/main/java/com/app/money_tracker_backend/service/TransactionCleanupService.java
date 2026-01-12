package com.app.money_tracker_backend.service;

import com.app.money_tracker_backend.model.Transaction;
import com.app.money_tracker_backend.repository.TransactionLogRepository;
import com.app.money_tracker_backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionCleanupService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    // This method will delete transactions and their logs
    @Transactional
    public void deleteOldTransactions() {
        // 30 days ago
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        // Find all deleted transactions older than 30 days
        List<Transaction> oldTransactions = transactionRepository
                .findAllByDeletedTrueAndUpdatedAtBefore(cutoffDate);

        for (Transaction tx : oldTransactions) {
            // Delete transaction logs first
            transactionLogRepository.deleteAllByTransactionId(tx.getId());

            // Delete the transaction
            transactionRepository.delete(tx);
        }
    }
}
