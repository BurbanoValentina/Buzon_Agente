package com.ucc.complaints.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "security_challenge")
public class SecurityChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String purpose;
    @Column(name = "value_hash", nullable = false)
    private String valueHash;
    private OffsetDateTime expiresAt;
    private OffsetDateTime consumedAt;
    protected SecurityChallenge() { }
}
