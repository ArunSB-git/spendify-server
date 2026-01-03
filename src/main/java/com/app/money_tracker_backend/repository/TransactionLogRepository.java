package com.app.money_tracker_backend.repository;

import com.app.money_tracker_backend.model.Transaction;
import com.app.money_tracker_backend.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {

    List<TransactionLog> findAllByUserId(UUID userId);


}
