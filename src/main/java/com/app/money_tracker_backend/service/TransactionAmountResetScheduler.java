package com.app.money_tracker_backend.service;

import com.app.money_tracker_backend.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class TransactionAmountResetScheduler {

    private final TransactionRepository transactionRepository;
    @Autowired
    private TransactionCleanupService cleanupService;

    public TransactionAmountResetScheduler(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /*
      Runs every day at 1:00 AM
      Cron format: second minute hour day month weekday
     */
/**
            0 0 1 * * ?
            │ │ │ │ │
            │ │ │ │ └── Any day of week
            │ │ │ │
            │ │ │ └──── Every month
            │ │ │
            │ │ └────── At 01 AM
            │ │
            │ └──────── 0 minutes
            │
            └────────── 0 seconds
*/

//    @Scheduled(cron = "0 */1 * * * ?")    //Testt for 1 min
    @Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Kolkata")
    @Transactional
    public void resetTransactionAmountsDaily() {
        int updatedRows = transactionRepository.resetAllTransactionAmounts();
        log.info("✅ Daily transaction reset completed. Rows updated: {}", updatedRows);
        cleanupService.deleteOldTransactions();
    }
}
