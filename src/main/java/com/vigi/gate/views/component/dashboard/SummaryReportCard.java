package com.vigi.gate.views.component.dashboard;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vigi.gate.dto.DailySummaryResponse;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.component.BaseCard;

public class SummaryReportCard extends BaseCard {
    
    private final VisitorManagementService visitorManagementService;
    
    // Indikator Utama (Angka Besar)
    private final H4 totalVisitValue = new H4("0");
    private final H4 insideAreaValue = new H4("0");
    
    // Indikator Breakdown Risiko (Badge Berwarna)
    private final Span greenBadge = new Span("GREEN: 0");
    private final Span yellowBadge = new Span("YELLOW: 0");
    private final Span redBadge = new Span("RED: 0");

    public SummaryReportCard(VisitorManagementService visitorManagementService) {
        super("");
        this.visitorManagementService = visitorManagementService;

        // --- HEADER LAYOUT ---
        H3 summaryHeaderTitle = new H3("Summary Report");
        summaryHeaderTitle.getStyle().set("margin", "0");

        HorizontalLayout summaryHeaderLayout = new HorizontalLayout(summaryHeaderTitle);
        summaryHeaderLayout.setWidthFull();
        summaryHeaderLayout.setAlignItems(Alignment.CENTER);

        // --- METRICS HUB / CARD GRID ---
        HorizontalLayout metricsGrid = new HorizontalLayout();
        metricsGrid.setWidthFull();
        metricsGrid.setSpacing(true);
        metricsGrid.getStyle().set("margin-top", "16px");

        // Box 1: Total Kunjungan
        VerticalLayout totalBox = createMetricBox("Total Kunjungan", totalVisitValue, VaadinIcon.USERS.create(), "#f8fafc");
        
        // Box 2: Masih di Area
        VerticalLayout insideBox = createMetricBox("Masih di Area", insideAreaValue, VaadinIcon.HOME.create(), "#f0fdf4");
        insideAreaValue.getStyle().set("color", "#15803d");

        // Box 3: Breakdown Risiko
        VerticalLayout riskBox = new VerticalLayout();
        riskBox.setWidth("34%");
        riskBox.getStyle()
            .set("background", "#ffa00005")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "8px")
            .set("padding", "12px")
            .set("box-sizing", "border-box");
        riskBox.setSpacing(false);
        
        Span riskLabel = new Span("Breakdown Risiko");
        riskLabel.getStyle().set("font-size", "12px").set("color", "#64748b").set("font-weight", "600");
        
        // Atur gaya badge risiko dasar
        styleRiskBadge(greenBadge, "#e6f4ea", "#137333");
        styleRiskBadge(yellowBadge, "#fef7e0", "#b06000");
        styleRiskBadge(redBadge, "#fce8e6", "#c5221f");

        HorizontalLayout badgesLayout = new HorizontalLayout(greenBadge, yellowBadge, redBadge);
        badgesLayout.setWidthFull();
        badgesLayout.getStyle().set("margin-top", "8px");
        badgesLayout.setSpacing(true);
        
        riskBox.add(riskLabel, badgesLayout);

        metricsGrid.add(totalBox, insideBox, riskBox);

        // Masukkan komponen ke dalam BaseCard layout
        add(summaryHeaderLayout, metricsGrid);

        // Memuat data awal saat komponen diinisialisasi
        refreshSummaryData();
    }

    public void refreshSummaryData() {
        DailySummaryResponse summary = visitorManagementService.generateDailySummary();
        
        // Update Nilai Angka
        totalVisitValue.setText(String.valueOf(summary.getTotalVisitToday()));
        insideAreaValue.setText(String.valueOf(summary.getTotalInsideArea()));
        
        // Update Nilai Badge Risiko
        greenBadge.setText("GREEN: " + summary.getGreenCount());
        yellowBadge.setText("YELLOW: " + summary.getYellowCount());
        redBadge.setText("RED: " + summary.getRedCount());
    }

    // Helper untuk membuat box metrik angka yang seragam
    private VerticalLayout createMetricBox(String label, H4 valueComponent, com.vaadin.flow.component.icon.Icon icon, String bgColor) {
        VerticalLayout box = new VerticalLayout();
        box.setWidth("33%");
        box.getStyle()
            .set("background-color", bgColor)
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "8px")
            .set("padding", "12px")
            .set("box-sizing", "border-box");
        box.setSpacing(false);

        Span titleLabel = new Span(label);
        titleLabel.getStyle().set("font-size", "12px").set("color", "#64748b").set("font-weight", "600");

        valueComponent.getStyle()
            .set("margin", "6px 0 0 0")
            .set("font-size", "24px")
            .set("font-weight", "800")
            .set("color", "#0f172a");

        HorizontalLayout contentRow = new HorizontalLayout(valueComponent);
        contentRow.setWidthFull();
        contentRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        contentRow.setAlignItems(Alignment.CENTER);
        
        if (icon != null) {
            icon.getStyle().set("color", "#94a3b8").set("font-size", "18px");
            contentRow.add(icon);
        }

        box.add(titleLabel, contentRow);
        return box;
    }

    // Helper untuk styling badge risiko individu
    private void styleRiskBadge(Span badge, String bg, String text) {
        badge.getStyle()
            .set("background-color", bg)
            .set("color", text)
            .set("font-weight", "700")
            .set("font-size", "11px")
            .set("padding", "4px 8px")
            .set("border-radius", "6px")
            .set("display", "inline-block")
            .set("text-align", "center")
            .set("flex", "1");
    }
    
}
