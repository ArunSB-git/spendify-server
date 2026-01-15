package com.app.money_tracker_backend.repository;

import com.app.money_tracker_backend.enums.TransactionType;
import com.app.money_tracker_backend.model.Transaction;
import com.app.money_tracker_backend.model.TransactionLog;
import com.app.money_tracker_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByUserIdAndDeletedFalseOrderByUpdatedAtDesc(UUID userId);


    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.amount = 0")
    int resetAllTransactionAmounts();

    // Find deleted transactions older than the given date
    List<Transaction> findAllByDeletedTrueAndUpdatedAtBefore(LocalDateTime dateTime);

    List<Transaction> findByUserIdAndTransactionTypeAndDeletedFalse(
            UUID userId,
            TransactionType transactionType
    );

    List<Transaction> findAllByUserIdAndDeletedFalse(UUID userId);

    void deleteByUserId(UUID userId);

}
