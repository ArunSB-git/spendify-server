package com.app.money_tracker_backend.repository;

import com.app.money_tracker_backend.model.Transaction;
import com.app.money_tracker_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByUserId(UUID userId);



}
