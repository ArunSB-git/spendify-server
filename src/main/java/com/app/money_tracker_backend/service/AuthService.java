package com.app.money_tracker_backend.service;

import com.app.money_tracker_backend.config.SecurityUtil;
import com.app.money_tracker_backend.dto.UserProfileResponse;
import com.app.money_tracker_backend.model.Bank;
import com.app.money_tracker_backend.model.User;
import com.app.money_tracker_backend.repository.BankRepository;
import com.app.money_tracker_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BankRepository bankRepository;


    public AuthService(UserRepository userRepository,BankRepository bankRepository) {
        this.userRepository = userRepository;
        this.bankRepository=bankRepository;

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
}
