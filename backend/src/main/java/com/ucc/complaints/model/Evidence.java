package com.ucc.complaints.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evidence")
public class Evidence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
    @Column(name = "drive_file_id", nullable = false)
    private String driveFileId;
    public Evidence() { }
    public String getDriveFileId() { return driveFileId; }
    public String getFileName() { return fileName; }
}
