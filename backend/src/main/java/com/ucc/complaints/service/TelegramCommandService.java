package com.ucc.complaints.service;

import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.TelegramLinkRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Locale;

@Service
public class TelegramCommandService {
    private final TelegramLinkRepository links;
    private final ComplaintRepository complaints;

    public TelegramCommandService(TelegramLinkRepository links, ComplaintRepository complaints) {
        this.links = links;
        this.complaints = complaints;
    }

    public String execute(String accountId, String command) {
        String normalized = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
        if ("/ayuda".equals(normalized) || "ayuda".equals(normalized)) {
            return "Comandos disponibles: Ayuda, Resumen, Pendientes, Mes. Solo lectura.";
        }
        String hash = accountId == null ? "" : accountId;
        if (links.findFirstByActiveTrue().isEmpty()
            || !links.findFirstByActiveTrue().get().getTelegramAccountHash().equals(hash)) {
            return "Cuenta no vinculada.";
        }
        return switch (normalized) {
            case "/ayuda", "ayuda" -> "Comandos disponibles: Ayuda, Resumen, Pendientes, Mes. Solo lectura.";
            case "/resumen", "resumen" -> "Resumen: " + complaints.count() + " quejas registradas.";
            case "/pendientes", "pendientes" -> "Pendientes: "
                + complaints.findByStatus(ComplaintStatus.PENDIENTE).size() + ".";
            case "/mes", "mes" -> "Quejas de " + YearMonth.now() + ": "
                + complaints.findAll().stream()
                    .filter(complaint -> YearMonth.from(complaint.getRegisteredAt()).equals(YearMonth.now()))
                    .count() + ".";
            default -> "Comando no reconocido. Usa Ayuda.";
        };
    }
}
