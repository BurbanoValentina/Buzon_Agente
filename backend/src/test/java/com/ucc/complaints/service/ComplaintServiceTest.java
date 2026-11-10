package com.ucc.complaints.service;

import com.ucc.complaints.dto.publiccomplaint.PublicComplaintRequest;
import com.ucc.complaints.dto.publiccomplaint.PublicComplaintResponse;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.repository.ComplaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {
    @Mock
    private ComplaintRepository repository;

    private ComplaintService service;

    @BeforeEach
    void setUp() {
        service = new ComplaintService(repository);
    }

    @Test
    void createsAnonymousComplaintSuccessfully() {
        PublicComplaintRequest request = new PublicComplaintRequest(
            "anonymous",
            null,
            null,
            "Infraestructura",
            null,
            "Falla en iluminacion aula",
            "La iluminacion parpadea continuamente durante la noche en el edificio principal.",
            true
        );

        PublicComplaintResponse response = service.create(request, null);

        assertNotNull(response.complaintId());
        assertEquals("Pendiente", response.status());
        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(repository).save(captor.capture());
        Complaint saved = captor.getValue();
        assertTrue(saved.isAnonymous());
        assertEquals("Infraestructura", saved.getCategory());
        assertNull(saved.getName());
    }

    @Test
    void createsIdentifiedComplaintWithEvidence() {
        PublicComplaintRequest request = new PublicComplaintRequest(
            "identified",
            "Carlos Lopez",
            "5",
            "Laboratorio",
            "Lab-204",
            "Equipos sin software especializado",
            "Los computadores del laboratorio 204 no tienen instaladas las herramientas requeridas.",
            true
        );

        MockMultipartFile file = new MockMultipartFile("evidence", "foto.png", "image/png", new byte[]{1, 2, 3});
        PublicComplaintResponse response = service.create(request, List.of(file));

        assertNotNull(response.complaintId());
        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(repository).save(captor.capture());
        Complaint saved = captor.getValue();
        assertFalse(saved.isAnonymous());
        assertEquals("Carlos Lopez", saved.getName());
        assertEquals("5", saved.getSemester());
    }

    @Test
    void rejectsInvalidUserType() {
        PublicComplaintRequest request = new PublicComplaintRequest(
            "otro", null, null, "Infraestructura", null, "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(request, null));
    }

    @Test
    void rejectsIdentifiedWithoutNameOrSemester() {
        PublicComplaintRequest req1 = new PublicComplaintRequest(
            "identified", "", "5", "Infraestructura", null, "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(req1, null));

        PublicComplaintRequest req2 = new PublicComplaintRequest(
            "identified", "Juan", " ", "Infraestructura", null, "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(req2, null));
    }

    @Test
    void rejectsInvalidCategoryOrMissingLocationNumber() {
        PublicComplaintRequest req1 = new PublicComplaintRequest(
            "anonymous", null, null, "CategoriaInvalida", null, "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(req1, null));

        PublicComplaintRequest req2 = new PublicComplaintRequest(
            "anonymous", null, null, "Laboratorio", " ", "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(req2, null));

        PublicComplaintRequest req3 = new PublicComplaintRequest(
            "anonymous", null, null, "Laboratorio", "A".repeat(51), "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(req3, null));
    }

    @Test
    void rejectsInvalidSubjectOrDescriptionOrPrivacy() {
        PublicComplaintRequest shortSub = new PublicComplaintRequest(
            "anonymous", null, null, "Bienestar", null, "Cort", "Descripcion con longitud valida para pruebas.", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(shortSub, null));

        PublicComplaintRequest shortDesc = new PublicComplaintRequest(
            "anonymous", null, null, "Bienestar", null, "Asunto valido", "Demasiado corta", true
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(shortDesc, null));

        PublicComplaintRequest noPriv = new PublicComplaintRequest(
            "anonymous", null, null, "Bienestar", null, "Asunto valido", "Descripcion con longitud valida para pruebas.", false
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(noPriv, null));
    }

    @Test
    void rejectsExcessFiles() {
        PublicComplaintRequest request = new PublicComplaintRequest(
            "anonymous", null, null, "Bienestar", null, "Asunto valido", "Descripcion con longitud valida para pruebas.", true
        );
        List<MultipartFile> files = List.of(
            new MockMultipartFile("f1", "1.png", "image/png", new byte[]{1}),
            new MockMultipartFile("f2", "2.png", "image/png", new byte[]{1}),
            new MockMultipartFile("f3", "3.png", "image/png", new byte[]{1}),
            new MockMultipartFile("f4", "4.png", "image/png", new byte[]{1}),
            new MockMultipartFile("f5", "5.png", "image/png", new byte[]{1}),
            new MockMultipartFile("f6", "6.png", "image/png", new byte[]{1})
        );
        assertThrows(ComplaintValidationException.class, () -> service.create(request, files));
    }
}
