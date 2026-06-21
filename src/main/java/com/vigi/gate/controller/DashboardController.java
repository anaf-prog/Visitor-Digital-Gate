package com.vigi.gate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vigi.gate.service.VisitorManagementService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final VisitorManagementService visitorManagementService;

    @GetMapping({ "/", "/dashboard" })
    public String dashboard(Model model) {
        model.addAttribute("activeVisitors", visitorManagementService.getActiveVisitors());
        model.addAttribute("todaySummary", visitorManagementService.generateDailySummary());
        return "dashboard";
    }

    @GetMapping("/visitor-history")
    public String visitorHistory() {
        return "visitor-history";
    }
}
