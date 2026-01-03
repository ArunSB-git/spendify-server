package com.app.money_tracker_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLog {


    @Id
    @GeneratedValue
    private UUID id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "transaction_id")
//    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "transaction_name", nullable = false)
    private String transactionName;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id") // 🔹 New foreign key
    private Bank bank;
}

