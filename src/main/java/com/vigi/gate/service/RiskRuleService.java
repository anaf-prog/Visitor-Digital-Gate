package com.vigi.gate.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vigi.gate.dto.RiskRuleForm;
import com.vigi.gate.entity.RiskRule;
import com.vigi.gate.repository.RiskRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskRuleService {

    private final RiskRuleRepository riskRuleRepository;

    @Transactional(readOnly = true)
    public List<RiskRule> getAllRules() {
        return riskRuleRepository.findAll()
            .stream()
            .sorted((a, b) -> {
                if (a.isActive() == b.isActive()) {
                    return Long.compare(a.getId(), b.getId());
                }
                return a.isActive() ? -1 : 1;
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public RiskRule getRuleById(Long id) {
        return riskRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Risk rule tidak ditemukan"));
    }

    @Transactional
    public void createRule(RiskRuleForm form) {
        RiskRule rule = new RiskRule();
        applyForm(rule, form);
        riskRuleRepository.save(rule);
    }

    @Transactional
    public void updateRule(Long id, RiskRuleForm form) {
        RiskRule rule = getRuleById(id);
        applyForm(rule, form);
        riskRuleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!riskRuleRepository.existsById(id)) {
            log.warn("Risk rule dengan id {} tidak ditemukan", id);
            throw new IllegalArgumentException("Risk rule tidak ditemukan");
        }
        riskRuleRepository.deleteById(id);
    }

    public RiskRuleForm toForm(RiskRule rule) {
        RiskRuleForm form = new RiskRuleForm();
        form.setRuleName(rule.getRuleName());
        form.setConditionType(rule.getConditionType());
        form.setConditionValue(rule.getConditionValue());
        form.setRiskLevel(rule.getRiskLevel());
        form.setScoreContribution(rule.getScoreContribution());
        form.setActive(rule.isActive());
        return form;
    }

    private void applyForm(RiskRule rule, RiskRuleForm form) {
        rule.setRuleName(form.getRuleName());
        rule.setConditionType(form.getConditionType().trim().toUpperCase());
        rule.setConditionValue(form.getConditionValue().trim());
        rule.setRiskLevel(form.getRiskLevel());
        rule.setScoreContribution(form.getScoreContribution());
        rule.setActive(form.isActive());
    }
}
