package com.vigi.gate.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.vigi.gate.entity.RiskRule;
import com.vigi.gate.repository.RiskRuleRepository;
import com.vigi.gate.repository.VisitorLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorRiskService {

    private final VisitorLogRepository visitorLogRepository;
    private final RiskRuleRepository riskRuleRepository;

    /**
     * Record untuk menampung hasil evaluasi risiko.
     * @param totalScore Skor total risiko yang dihitung
     * @param reason Alasan atau rincian dari skor risiko tersebut
     */
    public record RiskEvaluation(int totalScore, String reason) {
    }

    /**
     * Mengevaluasi risiko pengunjung berdasarkan aturan yang aktif di database.
     * Jika tidak ada aturan aktif, akan menggunakan metode fallback.
     * 
     * @param visitorId ID unik pengunjung
     * @param visitorNik NIK pengunjung untuk validasi tambahan
     * @param visitTime Waktu saat pengunjung melakukan check-in
     */
    public RiskEvaluation evaluateRisk(Long visitorId, String visitorNik, LocalDateTime visitTime) {
        log.info("Memulai evaluasi risiko untuk visitorId: {} pada waktu: {}", visitorId, visitTime);
        
        List<RiskRule> activeRules = riskRuleRepository.findByActiveTrue();
        if (activeRules.isEmpty()) {
            log.info("Tidak ada aturan risiko aktif ditemukan, menggunakan evaluasi fallback untuk visitorId: {}", visitorId);
            return evaluateRiskWithFallback(visitorId, visitTime);
        }

        long visitCount7Days = countVisitLast7Days(visitorId, visitTime);
        int hour = visitTime.getHour();
        int totalScore = 0;
        List<String> reasons = new ArrayList<>();

        // Setiap aturan yang cocok (match) akan menyumbangkan skor ke totalScore.
        // Seorang pengunjung bisa terkena lebih dari satu aturan sekaligus (misal: datang malam + sering datang).
        for (RiskRule rule : activeRules) {
            if (ruleMatches(rule, visitCount7Days, hour, visitorNik)) {
                int score = rule.getScoreContribution() == null ? 0 : Math.max(rule.getScoreContribution(), 0);
                totalScore += score;
                reasons.add(rule.getRuleName() + " (+" + score + ")");
            }
        }

        if (reasons.isEmpty()) {
            log.info("Evaluasi selesai: Tidak ada aturan yang cocok untuk visitorId: {}", visitorId);
            return new RiskEvaluation(0, "Tidak ada rule aktif yang match untuk kunjungan ini");
        }
        
        String finalReason = String.join(" | ", reasons);
        log.info("Evaluasi sukses untuk visitorId: {}. Total skor: {}, Alasan: {}", visitorId, totalScore, finalReason);
        return new RiskEvaluation(totalScore, finalReason);
    }

    /**
     * Evaluasi risiko cadangan menggunakan logika default aplikasi.
     * Digunakan ketika tidak ada aturan dinamis yang dikonfigurasi.
     * 
     * @param visitorId ID unik pengunjung
     * @param visitTime Waktu kunjungan
     * @return Objek RiskEvaluation berdasarkan logika fallback
     */
    public RiskEvaluation evaluateRiskWithFallback(Long visitorId, LocalDateTime visitTime) {
        int frequencyScore = calculateFrequencyScoreFallback(visitorId, visitTime);
        int hourScore = calculateVisitHourScoreFallback(visitTime.getHour());
        return new RiskEvaluation(frequencyScore + hourScore, buildFallbackReason(frequencyScore, hourScore));
    }

    /**
     * Menghitung jumlah kunjungan yang dilakukan pengunjung dalam 7 hari terakhir.
     * 
     * @param visitorId ID unik pengunjung
     * @param visitTime Waktu referensi kunjungan
     */
    public long countVisitLast7Days(Long visitorId, LocalDateTime visitTime) {
        LocalDateTime start = visitTime.toLocalDate().minusDays(7).atStartOfDay();
        return visitorLogRepository.countByVisitorIdAndCheckinTimeBetween(visitorId, start, visitTime);
    }

    /**
     * Memeriksa apakah sebuah aturan risiko cocok dengan parameter kunjungan saat ini.
     * 
     * @param rule Aturan risiko yang akan dicek
     * @param visitCount7Days Frekuensi kunjungan 7 hari terakhir
     * @param hour Jam kunjungan (0-23)
     * @param visitorNik NIK pengunjung
     */
    private boolean ruleMatches(RiskRule rule, long visitCount7Days, int hour, String visitorNik) {
        if (rule.getConditionType() == null || rule.getConditionValue() == null) {
            return false;
        }

        String type = rule.getConditionType().trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "FREQUENCY" -> matchFrequency(rule.getConditionValue(), visitCount7Days);
            case "TIME" -> matchTime(rule.getConditionValue(), hour);
            default -> false;
        };
    }

    /**
     * Mencocokkan kondisi frekuensi kunjungan dengan nilai ambang batas.
     */
    private boolean matchFrequency(String conditionValue, long visitCount) {
        try {
            int threshold = Integer.parseInt(conditionValue.trim());
            return visitCount >= threshold;
        } catch (NumberFormatException ex) {
            log.error("Kesalahan format nilai frekuensi: {}. Menggunakan nilai default false.", conditionValue);
            return false;
        }
    }

    /**
     * Mencocokkan jam kunjungan dengan rentang waktu yang ditentukan (format: HH-HH).
     */
    private boolean matchTime(String conditionValue, int hour) {
        String[] range = conditionValue.trim().split("-");
        if (range.length != 2) {
            log.warn("Format rentang waktu tidak valid: {}. Harusnya 'START-END' (contoh: 22-05)", conditionValue);
            return false;
        }
        try {
            int start = Integer.parseInt(range[0].trim());
            int end = Integer.parseInt(range[1].trim());

            LocalTime visit = LocalTime.of(hour, 0);
            LocalTime startTime = LocalTime.of(start, 0);
            LocalTime endTime = LocalTime.of(end, 0);

            if (start == end) {
                return true;
            }
            if (start < end) {
                return !visit.isBefore(startTime) && visit.isBefore(endTime);
            }
            return !visit.isBefore(startTime) || visit.isBefore(endTime);
        } catch (Exception ex) {
            log.error("Gagal mengevaluasi kondisi waktu: {}. Error: {}", conditionValue, ex.getMessage());
            return false;
        }
    }

    /**
     * Menghitung skor frekuensi kunjungan secara fallback.
     */
    private int calculateFrequencyScoreFallback(Long visitorId, LocalDateTime visitTime) {
        long visitCount = countVisitLast7Days(visitorId, visitTime);

        if (visitCount >= 10) {
            return 50;
        }
        if (visitCount >= 5) {
            return 30;
        }
        return 10;
    }

    /**
     * Menghitung skor berdasarkan jam kunjungan secara fallback.
     */
    private int calculateVisitHourScoreFallback(int hour) {
        if (hour >= 22 || hour < 5) {
            return 50;
        }
        if (hour >= 20 || hour < 7) {
            return 30;
        }
        return 10;
    }

    /**
     * Membangun string alasan untuk hasil evaluasi fallback.
     */
    private String buildFallbackReason(int frequencyScore, int hourScore) {
        return "Frekuensi kunjungan 7 hari terakhir: +" + frequencyScore + " | Skor jam kunjungan: +" + hourScore;
    }
    
}
