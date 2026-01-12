package com.app.money_tracker_backend.dto;

import com.app.money_tracker_backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String transactionName;
    private TransactionType transactionType;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer bankId;
}
