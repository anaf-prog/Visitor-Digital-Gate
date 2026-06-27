package com.vigi.gate.views.component;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.enumlevel.RiskLevel;

public class BaseCard extends VerticalLayout {

     public BaseCard(String title) {
        // Mengubah BaseCard ke struktur dark mode
        getStyle().set("background", "#111827")
            .set("border", "1px solid rgba(255, 255, 255, 0.05)")
            .set("border-radius", "14px")
            .set("box-shadow", "0 4px 20px rgba(0, 0, 0, 0.4)")
            .set("padding", "20px");
        setPadding(true);
        setSpacing(true);

        if (title != null && !title.isEmpty()) {
            H3 header = new H3(title);
            header.getStyle().set("margin-top", "0")
                .set("color", "#f3f4f6")
                .set("font-weight", "700");
            add(header);
        }
    }

    protected Span createRiskBadge(VisitorLogResponse logResponse) {
        Span badge = new Span(logResponse.getRiskLevel() != null ? logResponse.getRiskLevel().name() : "GREEN");
        // Penyesuaian warna badge resiko yang kontras untuk lingkungan gelap
        if (logResponse.getRiskLevel() == RiskLevel.RED) {
            badge.getStyle().set("color", "#f87171").set("font-weight", "800").set("text-shadow", "0 0 8px rgba(248, 113, 113, 0.3)");
        } else if (logResponse.getRiskLevel() == RiskLevel.YELLOW) {
            badge.getStyle().set("color", "#fbbf24").set("font-weight", "800").set("text-shadow", "0 0 8px rgba(251, 191, 36, 0.3)");
        } else {
            badge.getStyle().set("color", "#34d399").set("font-weight", "800").set("text-shadow", "0 0 8px rgba(52, 211, 153, 0.3)");
        }
        return badge;
    }
    
}
