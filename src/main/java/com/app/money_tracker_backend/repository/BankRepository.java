package com.app.money_tracker_backend.repository;

import com.app.money_tracker_backend.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
    // No extra methods needed for fetching all banks
}