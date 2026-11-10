package com.ucc.complaints.controller;

import com.ucc.complaints.dto.dashboard.DashboardResponse;
import com.ucc.complaints.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse dashboard(
        @RequestParam(defaultValue = "MONTHLY") String period,
        @RequestParam(required = false) OffsetDateTime from,
        @RequestParam(required = false) OffsetDateTime to) {
        return dashboardService.getDashboard(period, from, to);
    }
}
