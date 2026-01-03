package com.app.money_tracker_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransactionRequest {
    private UUID transactionId;  // can be null for creation
    private String transactionName;
    private String transactionType;
    private BigDecimal amount;
    private Integer bankId;
}
