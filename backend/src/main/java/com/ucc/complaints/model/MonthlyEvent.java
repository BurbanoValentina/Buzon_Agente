package com.ucc.complaints.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "monthly_event")
public class MonthlyEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;
    private String eventType;
    private LocalDate eventMonth;
    private OffsetDateTime occurredAt;
    protected MonthlyEvent() { }
    public MonthlyEvent(Complaint complaint, String eventType, LocalDate eventMonth,
                        OffsetDateTime occurredAt) {
        this.complaint = complaint;
        this.eventType = eventType;
        this.eventMonth = eventMonth;
        this.occurredAt = occurredAt;
    }
    public String getEventType() { return eventType; }
    public Complaint getComplaint() { return complaint; }
    public LocalDate getEventMonth() { return eventMonth; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
}
