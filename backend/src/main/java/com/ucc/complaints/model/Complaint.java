package com.ucc.complaints.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "complaint")
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;
    private boolean anonymous;
    private String name;
    private String semester;
    private String category;
    @Column(name = "location_number")
    private String locationNumber;
    private String subject;
    private String description;
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.PENDIENTE;
    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    protected Complaint() { }
    public Complaint(String publicId, boolean anonymous, String category, String subject,
                     String description, OffsetDateTime registeredAt) {
        this.publicId = publicId;
        this.anonymous = anonymous;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.status = ComplaintStatus.PENDIENTE;
        this.registeredAt = registeredAt;
    }
    public Long getId() { return id; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocationNumber() { return locationNumber; }
    public void setLocationNumber(String locationNumber) { this.locationNumber = locationNumber; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }
    public OffsetDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(OffsetDateTime registeredAt) { this.registeredAt = registeredAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
