package com.ucc.complaints.service;

import com.ucc.complaints.model.TelegramLink;
import com.ucc.complaints.repository.TelegramLinkRepository;
import com.ucc.complaints.telegram.TelegramCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class TelegramLinkService {
    private final TelegramLinkRepository repository;
    private final AuditService auditService;
    private final TelegramCryptoService cryptoService;

    public TelegramLinkService(TelegramLinkRepository repository, AuditService auditService) {
        this(repository, auditService, new TelegramCryptoService());
    }

    @Autowired
    public TelegramLinkService(TelegramLinkRepository repository,
                               AuditService auditService,
                               TelegramCryptoService cryptoService) {
        this.repository = repository;
        this.auditService = auditService;
        this.cryptoService = cryptoService;
    }

    @Transactional
    public TelegramLinkStatus link(String accountHash) {
        return link(accountHash, null);
    }

    @Transactional
    public TelegramLinkStatus link(String accountHash, String rawChatId) {
        repository.findFirstByActiveTrue().ifPresent(current -> {
            current.setActive(false);
            current.setUnlinkedAt(OffsetDateTime.now());
            repository.save(current);
        });
        TelegramLink linked = repository.findByTelegramAccountHash(accountHash)
            .orElseGet(() -> new TelegramLink(accountHash, OffsetDateTime.now()));
        linked.setActive(true);
        linked.setNotificationsEnabled(true);
        linked.setUnlinkedAt(null);
        if (rawChatId != null && !rawChatId.isBlank()) {
            linked.setEncryptedChatId(cryptoService.encrypt(rawChatId));
        }
        repository.save(linked);
        auditService.record("TELEGRAM_LINK", "administrator");
        return status();
    }

    @Transactional
    public TelegramLinkStatus unlink() {
        repository.findFirstByActiveTrue().ifPresent(link -> {
            link.setActive(false);
            link.setNotificationsEnabled(false);
            link.setUnlinkedAt(OffsetDateTime.now());
            repository.save(link);
        });
        auditService.record("TELEGRAM_UNLINK", "administrator");
        return status();
    }

    public TelegramLinkStatus status() {
        return repository.findFirstByActiveTrue()
            .map(link -> new TelegramLinkStatus(true, link.isNotificationsEnabled()))
            .orElseGet(() -> new TelegramLinkStatus(false, false));
    }

    public Optional<String> resolveActiveChatId() {
        return repository.findFirstByActiveTrue()
            .flatMap(link -> cryptoService.decrypt(link.getEncryptedChatId()));
    }

    public Optional<String> activeAccountHash() {
        return repository.findFirstByActiveTrue()
            .map(TelegramLink::getTelegramAccountHash);
    }

    @Transactional
    public TelegramLinkStatus setNotifications(boolean enabled) {
        repository.findFirstByActiveTrue().ifPresent(link -> {
            link.setNotificationsEnabled(enabled);
            repository.save(link);
        });
        auditService.record("TELEGRAM_NOTIFICATIONS_UPDATE", "administrator");
        return status();
    }

    public record TelegramLinkStatus(boolean linked, boolean notificationsEnabled) {}
}
