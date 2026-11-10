package com.ucc.complaints.foundation;

import com.ucc.complaints.AcademicComplaintsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AcademicComplaintsApplication.class,
    properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:complaints",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    })
class ApplicationContextTest {
    @Test
    void contextLoads() {
    }
}
