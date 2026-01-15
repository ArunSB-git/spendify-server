package com.app.money_tracker_backend.service;

import com.app.money_tracker_backend.config.SecurityUtil;
import com.app.money_tracker_backend.dto.UserProfileResponse;
import com.app.money_tracker_backend.model.Bank;
import com.app.money_tracker_backend.model.User;
import com.app.money_tracker_backend.repository.BankRepository;
import com.app.money_tracker_backend.repository.TransactionLogRepository;
import com.app.money_tracker_backend.repository.TransactionRepository;
import com.app.money_tracker_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;



@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final TransactionRepository transactionRepository;


    public AuthService(UserRepository userRepository,BankRepository bankRepository,TransactionLogRepository transactionLogRepository,TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.bankRepository=bankRepository;
        this.transactionRepository=transactionRepository;
        this.transactionLogRepository=transactionLogRepository;

    }

    // 🔹 Get the currently logged-in user
    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Bank> getAllBanks() {
        return bankRepository.findAll();
    }



    public UserProfileResponse getCurrentUserProfile() {

        // 🔐 get email from security context
        String email = SecurityUtil.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public void deleteAccount() {

        User user = getCurrentUser();
        UUID userId = user.getId();

        // 1️⃣ Delete all transaction logs
        transactionLogRepository.deleteByUserId(userId);

        // 2️⃣ Delete all transactions
        transactionRepository.deleteByUserId(userId);

        // 3️⃣ Delete user
        userRepository.delete(user);
    }
}
