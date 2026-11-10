package com.ucc.complaints.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "administrator_account")
public class AdministratorAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "security_answer_hash", nullable = false)
    private String securityAnswerHash;
    private int failedAttempts;
    private OffsetDateTime lockedUntil;
    protected AdministratorAccount() { }
    public AdministratorAccount(String username, String passwordHash, String securityAnswerHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.securityAnswerHash = securityAnswerHash;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSecurityAnswerHash() { return securityAnswerHash; }
    public int getFailedAttempts() { return failedAttempts; }
    public OffsetDateTime getLockedUntil() { return lockedUntil; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSecurityAnswerHash(String securityAnswerHash) { this.securityAnswerHash = securityAnswerHash; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public void setLockedUntil(OffsetDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
}
