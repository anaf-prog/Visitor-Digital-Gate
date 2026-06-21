package com.vigi.gate.views.component;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.enumlevel.RiskLevel;

public class BaseCard extends VerticalLayout {

    public BaseCard(String title) {
        getStyle().set("background", "#fff")
            .set("border-radius", "10px")
            .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.08)")
            .set("padding", "16px");
        setPadding(true);
        setSpacing(true);

        if (title != null && !title.isEmpty()) {
            H3 header = new H3(title);
            header.getStyle().set("margin-top", "0");
            add(header);
        }
    }

    protected Span createRiskBadge(VisitorLogResponse logResponse) {
        Span badge = new Span(logResponse.getRiskLevel() != null ? logResponse.getRiskLevel().name() : "GREEN");
        if (logResponse.getRiskLevel() == RiskLevel.RED) {
            badge.getStyle().set("color", "#b91c1c").set("font-weight", "700");
        } else if (logResponse.getRiskLevel() == RiskLevel.YELLOW) {
            badge.getStyle().set("color", "#b45309").set("font-weight", "700");
        } else {
            badge.getStyle().set("color", "#047857").set("font-weight", "700");
        }
        return badge;
    }
    
}
