package com.app.money_tracker_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bank_name", nullable = false, unique = true, length = 255)
    private String bankName;
}