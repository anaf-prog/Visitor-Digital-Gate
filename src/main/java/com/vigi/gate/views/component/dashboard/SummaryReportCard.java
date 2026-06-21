package com.vigi.gate.views.component.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vigi.gate.dto.DailySummaryResponse;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.component.BaseCard;

public class SummaryReportCard extends BaseCard {

    private final VisitorManagementService visitorManagementService;
    
    private final Span totalVisitSpan = new Span("0");
    private final Span insideAreaSpan = new Span("0");
    private final Span riskBreakdownSpan = new Span("Green: 0, Yellow: 0, Red: 0");
    private final Paragraph summaryParagraph = new Paragraph("");

    public SummaryReportCard(VisitorManagementService visitorManagementService) {
        super("");
        this.visitorManagementService = visitorManagementService;

        H3 summaryHeaderTitle = new H3("Summary Report");
        summaryHeaderTitle.getStyle().set("margin", "0");
        Button refreshSummaryBtn = new Button("Generate Summary", event -> refreshSummaryData());
        refreshSummaryBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout summaryHeaderLayout = new HorizontalLayout(summaryHeaderTitle, refreshSummaryBtn);
        summaryHeaderLayout.setWidthFull();
        summaryHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        summaryHeaderLayout.setAlignItems(Alignment.CENTER);

        Div summaryContent = new Div();
        summaryContent.getStyle().set("line-height", "1.6");
        summaryContent.add(new Paragraph(new Span("Total Kunjungan: "), totalVisitSpan));
        summaryContent.add(new Paragraph(new Span("Masih di Area: "), insideAreaSpan));
        summaryContent.add(new Paragraph(new Span("Breakdown Risiko: "), riskBreakdownSpan));
        summaryContent.add(summaryParagraph);

        add(summaryHeaderLayout, summaryContent);
    }

    public void refreshSummaryData() {
        DailySummaryResponse summary = visitorManagementService.generateDailySummary();
        totalVisitSpan.setText(String.valueOf(summary.getTotalVisitToday()));
        insideAreaSpan.setText(String.valueOf(summary.getTotalInsideArea()));
        riskBreakdownSpan.setText(String.format("Green: %d, Yellow: %d, Red: %d", summary.getGreenCount(), summary.getYellowCount(), summary.getRedCount()));
        summaryParagraph.setText(summary.getAiSummary());
    }
    
}
