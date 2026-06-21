package com.vigi.gate.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.vigi.gate.entity.RiskRule;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.repository.RiskRuleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskRuleSeeder implements CommandLineRunner {

    private final RiskRuleRepository riskRuleRepository;

    @Override
    public void run(String... args) {
        if (riskRuleRepository.count() > 0) {
            return;
        }

        /*
         * Seeder default hanya dijalankan saat tabel risk_rule masih kosong.
         *
         * Ringkasan konsep:
         * - TIME 22-05 (+50): check-in larut malam dini hari dianggap lebih berisiko.
         * - TIME 20-22 (+30): jam malam, risiko menengah.
         * - FREQUENCY 10 (+50): pengunjung yang sangat sering dalam 7 hari terakhir.
         * - FREQUENCY 5 (+30): pengunjung cukup sering dalam 7 hari terakhir.
         *
         * Setiap rule yang "match" akan dijumlahkan.
         * Jadi satu tamu bisa kena lebih dari satu rule sekaligus.
         */
        riskRuleRepository.saveAll(List.of(
                rule("Late Night Visit", "TIME", "22-05", RiskLevel.RED, 50, true),
                rule("Evening Visit", "TIME", "20-22", RiskLevel.YELLOW, 30, true),
                rule("Frequent Visitor 10+", "FREQUENCY", "10", RiskLevel.RED, 50, true),
                rule("Frequent Visitor 5+", "FREQUENCY", "5", RiskLevel.YELLOW, 30, true)
        ));
    }

    private RiskRule rule(String name,  String type, String value, RiskLevel level, int score, boolean active) {
        RiskRule rule = new RiskRule();
        rule.setRuleName(name);
        rule.setConditionType(type);
        rule.setConditionValue(value);
        rule.setRiskLevel(level);
        rule.setScoreContribution(score);
        rule.setActive(active);
        return rule;
    }
}
