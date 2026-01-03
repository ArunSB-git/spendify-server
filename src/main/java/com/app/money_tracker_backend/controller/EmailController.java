package com.app.money_tracker_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EmailController {

    @Autowired
    private JavaMailSender mailSender;

    public static class QueryRequest {
        public String to;
        public String subject;
        public String body;
    }

    @PostMapping("/send-query")
    public String sendQuery(@RequestBody QueryRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.to);
            message.setSubject(request.subject);
            message.setText(request.body);
            mailSender.send(message);
            return "Email sent successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send email: " + e.getMessage();
        }
    }
}