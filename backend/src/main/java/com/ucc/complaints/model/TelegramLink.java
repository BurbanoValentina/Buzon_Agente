package com.ucc.complaints.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "telegram_link")
public class TelegramLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_account_hash", nullable = false, unique = true)
    private String telegramAccountHash;
    @Column(name = "encrypted_chat_id")
    private String encryptedChatId;
    private OffsetDateTime linkedAt;
    private boolean active;
    private boolean notificationsEnabled = true;
    private OffsetDateTime unlinkedAt;

    protected TelegramLink() { }

    public TelegramLink(String telegramAccountHash, OffsetDateTime linkedAt) {
        this.telegramAccountHash = telegramAccountHash;
        this.linkedAt = linkedAt;
        this.active = true;
        this.notificationsEnabled = true;
    }

    public Long getId() { return id; }
    public String getTelegramAccountHash() { return telegramAccountHash; }
    public String getEncryptedChatId() { return encryptedChatId; }
    public void setEncryptedChatId(String encryptedChatId) { this.encryptedChatId = encryptedChatId; }
    public OffsetDateTime getLinkedAt() { return linkedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    public OffsetDateTime getUnlinkedAt() { return unlinkedAt; }
    public void setUnlinkedAt(OffsetDateTime unlinkedAt) { this.unlinkedAt = unlinkedAt; }
}
