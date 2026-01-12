package com.app.money_tracker_backend.controller;

import com.app.money_tracker_backend.dto.AddAmountRequest;
import com.app.money_tracker_backend.dto.TransactionLogResponse;
import com.app.money_tracker_backend.dto.TransactionRequest;
import com.app.money_tracker_backend.dto.TransactionResponse;
import com.app.money_tracker_backend.model.Transaction;
import com.app.money_tracker_backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ✅ Create transaction
    @PostMapping("/createTask")
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }
    // ✅ Update transaction
    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(
            @PathVariable String id,
            @RequestBody TransactionRequest request
    ) {
        return transactionService.updateTransaction(id, request);
    }

//    @DeleteMapping("/{transactionId}")
//    public ResponseEntity<Map<String, String>> deleteTransaction(
//            @PathVariable String transactionId) {
//
//        return (ResponseEntity<Map<String, String>>) ResponseEntity.badRequest();
//    }

    @GetMapping("/logs")
    public List<TransactionLogResponse> getAllTransactionsLogs() {
        return transactionService.getAllTransactionsLogs();
    }

    @PostMapping("/add-amount")
    public TransactionResponse addAmountToTransaction(
            @RequestBody AddAmountRequest request
    ) {
        return transactionService.addAmountToTransaction(request);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable UUID transactionId
    ) {
        transactionService.deleteTransaction(transactionId);

        return ResponseEntity.ok(
                Map.of("message", "Transaction deleted successfully")
        );
    }


}
