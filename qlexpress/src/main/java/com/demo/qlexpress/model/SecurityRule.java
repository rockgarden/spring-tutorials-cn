package com.demo.qlexpress.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_rules")
@Data
public class SecurityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Lob
    private String script;
    private String description;
    private Boolean enabled = true;
    private Integer riskLevel = 3;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}