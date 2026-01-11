package com.app.money_tracker_backend.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class AddAmountRequest {
    private UUID transactionId;
    private BigDecimal amountToAdd;
}
