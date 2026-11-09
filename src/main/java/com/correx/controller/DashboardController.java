package com.correx.controller;

import com.correx.dto.DashboardResponse;
import com.correx.service.DashboardService;
import com.correx.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    public DashboardController(DashboardService dashboardService, SecurityUtils securityUtils) {
        this.dashboardService = dashboardService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(dashboardService.getDashboard(professorId));
    }
}
