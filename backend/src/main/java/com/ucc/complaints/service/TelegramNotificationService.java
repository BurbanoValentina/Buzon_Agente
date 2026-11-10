package com.ucc.complaints.service;

import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.TelegramLink;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.TelegramLinkRepository;
import com.ucc.complaints.telegram.TelegramClient;
import com.ucc.complaints.telegram.TelegramCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class TelegramNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramNotificationService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TelegramLinkRepository links;
    private final TelegramClient client;
    private final TelegramCryptoService cryptoService;
    private ComplaintRepository complaints;

    public TelegramNotificationService(TelegramLinkRepository links, TelegramClient client) {
        this(links, client, new TelegramCryptoService());
    }

    @Autowired
    public TelegramNotificationService(TelegramLinkRepository links,
                                       TelegramClient client,
                                       TelegramCryptoService cryptoService) {
        this.links = links;
        this.client = client;
        this.cryptoService = cryptoService;
    }

    @Autowired(required = false)
    public void setComplaintRepository(ComplaintRepository complaints) {
        this.complaints = complaints;
    }

    public void notifyNewComplaint(String publicId, String category) {
        try {
            links.findFirstByActiveTrue().filter(TelegramLink::isNotificationsEnabled).ifPresent(link -> {
                String safeCategory = sanitize(category);
                String safeId = sanitize(publicId);
                String targetChatId = resolveTargetChatId(link);
                String message = buildNotificationText(safeId, safeCategory);
                client.sendMessage(targetChatId, message);
            });
        } catch (RuntimeException exception) {
            LOGGER.warn("telegram_notification_failed=true; complaint remains registered");
        }
    }

    private String resolveTargetChatId(TelegramLink link) {
        if (link.getEncryptedChatId() != null && !link.getEncryptedChatId().isBlank() && cryptoService != null) {
            Optional<String> decrypted = cryptoService.decrypt(link.getEncryptedChatId());
            if (decrypted.isPresent() && !decrypted.get().isBlank()) {
                return decrypted.get();
            }
        }
        return link.getTelegramAccountHash();
    }

    private String buildNotificationText(String safeId, String safeCategory) {
        StringBuilder message = new StringBuilder();
        message.append("Nueva queja recibida.\n");
        message.append("ID: ").append(safeId).append('\n');
        message.append("Categoría: ").append(safeCategory);

        if (complaints != null && !safeId.isBlank()) {
            complaints.findByPublicId(safeId).ifPresent(complaint -> appendMetadata(message, complaint));
        } else {
            message.append(". Consulta el detalle en el panel.");
        }
        return message.toString();
    }

    private void appendMetadata(StringBuilder message, Complaint complaint) {
        if (complaint.getLocationNumber() != null && !complaint.getLocationNumber().isBlank()) {
            message.append('\n').append("Número: ").append(sanitize(complaint.getLocationNumber()));
        }
        message.append('\n').append("Modalidad: ").append(complaint.isAnonymous() ? "Anónima" : "Identificada");
        if (complaint.getRegisteredAt() != null) {
            message.append('\n').append("Fecha: ").append(complaint.getRegisteredAt().format(DATE_FORMAT));
        }
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n]", " ").trim();
    }
}
