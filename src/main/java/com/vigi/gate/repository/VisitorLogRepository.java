package com.vigi.gate.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vigi.gate.entity.VisitorLog;

public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long>, JpaSpecificationExecutor<VisitorLog> {

    long countByVisitorIdAndCheckinTimeBetween(Long visitorId, LocalDateTime start, LocalDateTime end);

    List<VisitorLog> findByCheckoutTimeIsNullOrderByCheckinTimeDesc();

    List<VisitorLog> findByCheckinTimeBetweenOrderByCheckinTimeDesc(LocalDateTime start, LocalDateTime end);
}
