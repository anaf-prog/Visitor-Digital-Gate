package com.vigi.gate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vigi.gate.entity.RiskRule;

public interface RiskRuleRepository extends JpaRepository<RiskRule, Long>, JpaSpecificationExecutor<RiskRule> {

    List<RiskRule> findByActiveTrue();
}
