package com.app.money_tracker_backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyTransactionSpentResponse(
        UUID transactionId,
        String transactionName,
        BigDecimal amount
) {}