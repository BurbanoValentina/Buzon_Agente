package com.ucc.complaints.dto.publiccomplaint;

public record PublicComplaintRequest(
    String userType,
    String name,
    String semester,
    String category,
    String locationNumber,
    String subject,
    String description,
    boolean privacyAccepted
) { }
