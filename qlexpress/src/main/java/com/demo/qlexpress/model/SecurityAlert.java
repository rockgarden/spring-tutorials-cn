package com.demo.qlexpress.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts")
@Data
public class SecurityAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ruleName;
    private String alertType;
    @Lob
    private String logEvent; // JSON string
    private LocalDateTime triggeredAt;
    private Boolean handled = false;
}