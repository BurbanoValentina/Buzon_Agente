package com.ucc.complaints.dto.admin;

import com.ucc.complaints.model.ComplaintStatus;
import java.time.OffsetDateTime;

public record ComplaintFilter(
    OffsetDateTime from,
    OffsetDateTime to,
    String category,
    ComplaintStatus status,
    Boolean anonymous
) { }
