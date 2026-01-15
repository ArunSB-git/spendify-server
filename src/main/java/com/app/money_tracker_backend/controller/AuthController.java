package com.app.money_tracker_backend.controller;

import com.app.money_tracker_backend.dto.UserProfileResponse;
import com.app.money_tracker_backend.model.Bank;
import com.app.money_tracker_backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "http://localhost:5174")
@RestController
public class AuthController {

    @GetMapping("/")
    public String greet() {
        return "Hello World";
    }


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }



    // ✅ Logged-in user profile API
    @GetMapping("/api/users/me")
    public UserProfileResponse getMyProfile() {
        return authService.getCurrentUserProfile();
    }

    @GetMapping("/api/session-check")
    public SessionResponse checkSession(HttpSession session) {
        boolean valid = session != null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null;
        return new SessionResponse(valid);
    }

    @GetMapping("api/banks")
    public List<Bank> getAllBanks() {
        return authService.getAllBanks();
    }

    static class SessionResponse {
        private boolean valid;

        public SessionResponse(boolean valid) { this.valid = valid; }

        public boolean isValid() { return valid; }

        public void setValid(boolean valid) { this.valid = valid; }
    }
    @PostMapping("/api/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate(); // destroys JSESSIONID
        }
        return "Logged out successfully";
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(HttpSession session) {
        authService.deleteAccount();
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok("Account deleted successfully");
    }
}
