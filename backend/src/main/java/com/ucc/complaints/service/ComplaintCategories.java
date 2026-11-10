package com.ucc.complaints.service;

import java.util.List;

public final class ComplaintCategories {
    public static final List<String> VALUES = List.of(
        "Infraestructura", "Bienestar", "Laboratorio", "Profesores", "Estudiantes",
        "Horarios", "Tecnología", "Administrativos", "Convivencia", "Aulas",
        "Otras situaciones"
    );

    private ComplaintCategories() { }
}
