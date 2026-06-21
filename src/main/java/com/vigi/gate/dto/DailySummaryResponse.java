package com.vigi.gate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailySummaryResponse {
    private int totalVisitToday;
    private int totalInsideArea;
    private int greenCount;
    private int yellowCount;
    private int redCount;
    private String aiSummary;
}
