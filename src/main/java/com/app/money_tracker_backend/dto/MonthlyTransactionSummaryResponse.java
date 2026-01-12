package com.app.money_tracker_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyTransactionSummaryResponse {

    private int month;
    private List<MonthlyTransactionRecordResponse> transactions;
}
