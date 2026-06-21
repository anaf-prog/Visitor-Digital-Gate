package com.vigi.gate.controller;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vigi.gate.dto.DailySummaryResponse;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.dto.VisitorRegistrationRequest;
import com.vigi.gate.service.VisitorManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
@Validated
public class VisitorController {

    private final VisitorManagementService visitorManagementService;

    @PostMapping("/register")
    public VisitorLogResponse register(
            @RequestParam("fullName") @NotBlank(message = "Nama wajib diisi") String fullName,
            @RequestParam("nik") @Pattern(regexp = "\\d{16}", message = "NIK harus 16 digit angka") String nik,
            @RequestParam("purpose") @NotBlank(message = "Tujuan kunjungan wajib diisi") String purpose,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        VisitorRegistrationRequest request = new VisitorRegistrationRequest();
        request.setFullName(fullName);
        request.setNik(nik);
        request.setPurpose(purpose);

        return visitorManagementService.registerVisitor(request, photo);
    }

    @PostMapping("/{logId}/checkout")
    public VisitorLogResponse checkout(@PathVariable("logId") Long logId) {
        return visitorManagementService.checkoutVisitor(logId);
    }

    @GetMapping("/active")
    public List<VisitorLogResponse> getActiveVisitors() {
        return visitorManagementService.getActiveVisitors();
    }

    @GetMapping("/today")
    public List<VisitorLogResponse> getTodayLogs() {
        return visitorManagementService.getTodayLogs();
    }

    @GetMapping("/history")
    public List<VisitorLogResponse> getHistoryLogs() {
        return visitorManagementService.getLast30DaysLogs();
    }

    @GetMapping("/summary")
    public DailySummaryResponse getSummary() {
        return visitorManagementService.generateDailySummary();
    }

    @DeleteMapping("/{logId}")
    public void deleteVisitor(@PathVariable("logId") Long logId) {
        visitorManagementService.deleteVisitor(logId);
    }
}
