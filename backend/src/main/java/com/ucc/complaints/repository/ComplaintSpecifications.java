package com.ucc.complaints.repository;

import com.ucc.complaints.dto.admin.ComplaintFilter;
import com.ucc.complaints.model.Complaint;
import java.util.function.Predicate;

public final class ComplaintSpecifications {
    private ComplaintSpecifications() { }

    public static Predicate<Complaint> matches(ComplaintFilter filter) {
        return complaint -> (filter == null || filter.from() == null
                || !complaint.getRegisteredAt().isBefore(filter.from()))
            && (filter == null || filter.to() == null
                || !complaint.getRegisteredAt().isAfter(filter.to()))
            && (filter == null || filter.category() == null || filter.category().isBlank()
                || filter.category().equals(complaint.getCategory()))
            && (filter == null || filter.status() == null || filter.status().equals(complaint.getStatus()))
            && (filter == null || filter.anonymous() == null || filter.anonymous() == complaint.isAnonymous());
    }
}
