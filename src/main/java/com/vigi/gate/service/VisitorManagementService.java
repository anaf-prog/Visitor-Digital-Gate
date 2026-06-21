package com.vigi.gate.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vigi.gate.dto.DailySummaryResponse;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.dto.VisitorRegistrationRequest;
import com.vigi.gate.entity.Visitor;
import com.vigi.gate.entity.VisitorLog;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.repository.VisitorLogRepository;
import com.vigi.gate.repository.VisitorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorManagementService {

    private final VisitorRepository visitorRepository;
    private final VisitorLogRepository visitorLogRepository;
    private final VisitorRiskService visitorRiskService;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public VisitorLogResponse registerVisitor(VisitorRegistrationRequest request) {
        return registerVisitor(request, null);
    }

    @Transactional
    public VisitorLogResponse registerVisitor(VisitorRegistrationRequest request, MultipartFile photo) {
        String photoUrl = request.getPhoto();
        
        if (photo != null && !photo.isEmpty()) {
            Map<String, String> uploadResult = cloudinaryService.uploadImage(photo, "visitor_photos");
            if (uploadResult != null) {
                photoUrl = uploadResult.get("url");
                log.info("Foto berhasil diupload: {}", photoUrl);
            } else {
                log.warn("Gagal upload foto, melanjutkan tanpa foto");
            }
        }
        
        // Simpan visitor dengan URL foto
        Visitor visitor = visitorRepository.findByNik(request.getNik()).orElseGet(Visitor::new);
        
        visitor.setNik(request.getNik());
        visitor.setFullName(request.getFullName());
        visitor.setPhoto(photoUrl);
        log.debug("Sebelum save - Photo URL yang akan disimpan: {}", photoUrl);
        
        Visitor savedVisitor = visitorRepository.save(visitor);
        
        log.debug("Setelah save - ID: {}, Photo yang tersimpan: {}", savedVisitor.getId(), savedVisitor.getPhoto());

        VisitorRiskService.RiskEvaluation riskEvaluation = visitorRiskService.evaluateRisk(savedVisitor.getId(), savedVisitor.getNik(), LocalDateTime.now());
        int totalScore = riskEvaluation.totalScore();
        RiskLevel riskLevel = mapScoreToRiskLevel(totalScore);

        VisitorLog visitorLog = new VisitorLog();
        visitorLog.setVisitor(savedVisitor);
        visitorLog.setPurpose(request.getPurpose());
        visitorLog.setCheckinTime(LocalDateTime.now());
        visitorLog.setRiskScore(totalScore);
        visitorLog.setRiskLevel(riskLevel);
        visitorLog.setRiskReason(riskEvaluation.reason());

        return toResponse(visitorLogRepository.save(visitorLog));
    }

    @Transactional
    public VisitorLogResponse checkoutVisitor(Long logId) {
        VisitorLog visitorLog = visitorLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Data kunjungan tidak ditemukan"));

        visitorLog.setCheckoutTime(LocalDateTime.now());
        return toResponse(visitorLogRepository.save(visitorLog));
    }

    @Transactional
    public void deleteVisitor(Long logId) {
        VisitorLog visitorLog = visitorLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Data kunjungan tidak ditemukan"));
        
        visitorLogRepository.delete(visitorLog);
    }

    @Transactional(readOnly = true)
    public List<VisitorLogResponse> getActiveVisitors() {
        return visitorLogRepository.findByCheckoutTimeIsNullOrderByCheckinTimeDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitorLogResponse> getTodayLogs() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return visitorLogRepository.findByCheckinTimeBetweenOrderByCheckinTimeDesc(start, end)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitorLogResponse> getLast30DaysLogs() {
        LocalDateTime end = LocalDate.now().atStartOfDay().plusDays(1);
        LocalDateTime start = end.minusDays(30);
        return visitorLogRepository.findByCheckinTimeBetweenOrderByCheckinTimeDesc(start, end)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DailySummaryResponse generateDailySummary() {
        List<VisitorLogResponse> todayLogs = getTodayLogs();
        int green = (int) todayLogs.stream().filter(v -> v.getRiskLevel() == RiskLevel.GREEN).count();
        int yellow = (int) todayLogs.stream().filter(v -> v.getRiskLevel() == RiskLevel.YELLOW).count();
        int red = (int) todayLogs.stream().filter(v -> v.getRiskLevel() == RiskLevel.RED).count();
        int inside = (int) todayLogs.stream().filter(VisitorLogResponse::isInsideArea).count();

        return DailySummaryResponse.builder()
            .totalVisitToday(todayLogs.size())
            .totalInsideArea(inside)
            .greenCount(green)
            .yellowCount(yellow)
            .redCount(red)
            .aiSummary(visitorRiskService.buildAiSummary(todayLogs.size(), inside, green, yellow, red))
            .build();
    }

    private RiskLevel mapScoreToRiskLevel(int score) {
        if (score >= 80) {
            return RiskLevel.RED;
        }
        if (score >= 50) {
            return RiskLevel.YELLOW;
        }
        return RiskLevel.GREEN;
    }

    private VisitorLogResponse toResponse(VisitorLog visitorLog) {
        return VisitorLogResponse.builder()
            .logId(visitorLog.getId())
            .fullName(visitorLog.getVisitor().getFullName())
            .nik(visitorLog.getVisitor().getNik())
            .photoUrl(visitorLog.getVisitor().getPhoto())
            .purpose(visitorLog.getPurpose())
            .checkinTime(visitorLog.getCheckinTime())
            .checkoutTime(visitorLog.getCheckoutTime())
            .riskLevel(visitorLog.getRiskLevel())
            .riskScore(visitorLog.getRiskScore())
            .riskReason(visitorLog.getRiskReason())
            .insideArea(visitorLog.getCheckoutTime() == null)
            .build();
    }
}