package com.ucc.complaints.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "word_report_draft")
public class WordReportDraft {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String content;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    protected WordReportDraft() { }
    public WordReportDraft(String title, String content) {
        OffsetDateTime now = OffsetDateTime.now();
        this.title = title;
        this.content = content;
        this.createdAt = now;
        this.updatedAt = now;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = OffsetDateTime.now();
    }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
