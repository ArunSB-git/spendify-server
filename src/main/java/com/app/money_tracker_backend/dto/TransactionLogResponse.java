package com.app.money_tracker_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransactionLogResponse {
    private UUID id;
    private String transactionName;
    private String transactionType;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private Integer bankId;
    private String action;
}
