package com.app.money_tracker_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MonthlyTransactionAmountResponse {

    private int month;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
}
