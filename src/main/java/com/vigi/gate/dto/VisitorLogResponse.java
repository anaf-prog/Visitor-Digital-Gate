package com.vigi.gate.dto;

import java.time.LocalDateTime;

import com.vigi.gate.enumlevel.RiskLevel;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorLogResponse {
    private Long logId;
    private String fullName;
    private String nik;
    private String photoUrl;
    private String purpose;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private RiskLevel riskLevel;
    private Integer riskScore;
    private String riskReason;
    private boolean insideArea;
}
