package com.ucc.complaints.telegram;

import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.TelegramLinkRepository;
import com.ucc.complaints.service.TelegramCommandService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramCommandServiceTest {
    @Test
    void helpDoesNotExposeComplaintData() {
        TelegramLinkRepository links = mock(TelegramLinkRepository.class);
        ComplaintRepository complaints = mock(ComplaintRepository.class);
        TelegramCommandService service = new TelegramCommandService(links, complaints);
        assertTrue(service.execute("unlinked", "Ayuda").contains("Solo lectura"));
    }
}
