package com.ucc.complaints.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "status_history")
public class StatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;
    private String previousStatus;
    private String newStatus;
    private String comment;
    private String changedBy;
    private OffsetDateTime changedAt;
    protected StatusHistory() { }
    public StatusHistory(Complaint complaint, ComplaintStatus previousStatus,
                        ComplaintStatus newStatus, String comment, String changedBy,
                        OffsetDateTime changedAt) {
        this.complaint = complaint;
        this.previousStatus = previousStatus == null ? null : previousStatus.name();
        this.newStatus = newStatus.name();
        this.comment = comment;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getComment() { return comment; }
    public String getChangedBy() { return changedBy; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
