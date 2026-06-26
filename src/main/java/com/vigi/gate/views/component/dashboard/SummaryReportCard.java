package com.vigi.gate.views.component.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
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
    
    // Kotak Narasi AI Summary
    private final Paragraph summaryParagraph = new Paragraph("Memuat data rangkuman...");

    public SummaryReportCard(VisitorManagementService visitorManagementService) {
        super("");
        this.visitorManagementService = visitorManagementService;

        // --- HEADER LAYOUT ---
        H3 summaryHeaderTitle = new H3("Summary Report");
        summaryHeaderTitle.getStyle().set("margin", "0");
        
        Button refreshSummaryBtn = new Button("Generate Summary", VaadinIcon.PLAY.create(), event -> refreshSummaryData());
        refreshSummaryBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        refreshSummaryBtn.getStyle().set("background-color", "#2563eb");

        HorizontalLayout summaryHeaderLayout = new HorizontalLayout(summaryHeaderTitle, refreshSummaryBtn);
        summaryHeaderLayout.setWidthFull();
        summaryHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
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

        VerticalLayout aiContainer = new VerticalLayout();
        aiContainer.setWidthFull();
        aiContainer.setPadding(true);
        aiContainer.setSpacing(false);
        aiContainer.getStyle()
            .set("background-color", "#f1f5f9")
            .set("border-left", "4px solid #3b82f6")
            .set("border-radius", "0 8px 8px 0")
            .set("margin-top", "16px")
            .set("box-sizing", "border-box"); 

        HorizontalLayout aiHeader = new HorizontalLayout();
        aiHeader.setAlignItems(Alignment.CENTER);
        aiHeader.setSpacing(true);
        
        var aiIcon = VaadinIcon.AUTOMATION.create();
        aiIcon.getStyle().set("color", "#2563eb").set("font-size", "18px");
        
        Span aiTitle = new Span("System Executive Summary");
        aiTitle.getStyle().set("font-weight", "700").set("color", "#1e293b").set("font-size", "14px");
        aiHeader.add(aiIcon, aiTitle);

        summaryParagraph.getStyle()
            .set("margin", "8px 0 0 0")
            .set("font-size", "13.5px")
            .set("color", "#334155")
            .set("line-height", "1.6");

        aiContainer.add(aiHeader, summaryParagraph);

        // Masukkan semua ke dalam BaseCard layout
        add(summaryHeaderLayout, metricsGrid, aiContainer);
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
        
        // Update Narasi AI
        if (summary.getAiSummary() != null && !summary.getAiSummary().isBlank()) {
            summaryParagraph.setText(summary.getAiSummary());
        } else {
            summaryParagraph.setText("Tidak ada aktivitas kunjungan mencurigakan terpantau hari ini. Sistem dalam kondisi aman.");
        }
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
