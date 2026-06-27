package com.vigi.gate.views.component.dashboard;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.MainView;
import com.vigi.gate.views.component.BaseCard;
import com.vigi.gate.views.component.GridPagination;

public class ActiveVisitorsCard extends BaseCard {
    
    private final VisitorManagementService visitorManagementService;
    MainView mainView;
    private final Grid<VisitorLogResponse> activeGrid = new Grid<>(VisitorLogResponse.class, false);
    private final GridPagination<VisitorLogResponse> pagination;

    public ActiveVisitorsCard(VisitorManagementService visitorManagementService, MainView mainView) {
        super("");
        this.visitorManagementService = visitorManagementService;
        this.mainView = mainView;
        this.pagination = new GridPagination<>(activeGrid); // Inisialisasi komponen paginasi

        getElement().setAttribute("theme", "dark");

        getStyle()
            .set("background-color", "#111827")
            .set("border", "1px solid rgba(0, 255, 102, 0.15)")
            .set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.05)")
            .set("border-radius", "12px")
            .set("padding", "20px")
            .set("color", "#f3f4f6");

        H3 activeHeaderTitle = new H3("Realtime Log - Active Visitors");
        activeHeaderTitle.getStyle().set("margin", "0").set("color", "#00ff66");
        
        Button refreshActiveBtn = new Button("Refresh", event -> refreshActiveData());
        refreshActiveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        refreshActiveBtn.getStyle()
            .set("background-color", "rgba(0, 255, 102, 0.1)")
            .set("color", "#00ff66")
            .set("border", "1px solid rgba(0, 255, 102, 0.3)")
            .set("font-weight", "600")
            .set("cursor", "pointer");
        
        HorizontalLayout activeHeaderLayout = new HorizontalLayout(activeHeaderTitle, refreshActiveBtn);
        activeHeaderLayout.setWidthFull();
        activeHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        activeHeaderLayout.setAlignItems(Alignment.CENTER);
        activeHeaderLayout.getStyle().set("margin-bottom", "16px");

        // Kustomisasi Styling Grid agar menyesuaikan dengan Dark Mode
        activeGrid.getElement().setAttribute("theme", "dark");
        activeGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        
        activeGrid.getStyle()
            .set("background-color", "#111827")
            .set("--lumo-base-color", "#111827")
            .set("--lumo-body-text-color", "#f3f4f6")
            .set("--lumo-header-text-color", "#00ff66")
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.03)")
            .set("--lumo-contrast-20pct", "rgba(0, 255, 102, 0.15)")
            .set("border", "1px solid rgba(0, 255, 102, 0.1)")
            .set("border-radius", "8px");

        activeGrid.addColumn(VisitorLogResponse::getFullName).setHeader("Nama").setSortable(true);
        activeGrid.addColumn(VisitorLogResponse::getNik).setHeader("NIK");
        activeGrid.addColumn(VisitorLogResponse::getPurpose).setHeader("Tujuan");
        activeGrid.addColumn(new ComponentRenderer<>(this::createRiskBadge)).setHeader("Risk");
        activeGrid.addColumn(row -> row.getRiskScore() != null ? row.getRiskScore() : "-").setHeader("Skor");
        
        activeGrid.addColumn(new ComponentRenderer<>(row -> {
            Button checkoutBtn = new Button("Checkout");
            checkoutBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
  
            checkoutBtn.getStyle()
                .set("background-color", "#059669")
                .set("color", "#090d16")
                .set("font-weight", "700")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out");

            checkoutBtn.getElement().addEventListener("mouseover", e -> {
                checkoutBtn.getStyle().set("background-color", "#00ff66");
                checkoutBtn.getStyle().set("box-shadow", "0 0 10px rgba(0, 255, 102, 0.5)");
            });
            checkoutBtn.getElement().addEventListener("mouseout", e -> {
                checkoutBtn.getStyle().set("background-color", "#059669");
                checkoutBtn.getStyle().remove("box-shadow");
            });

            checkoutBtn.addClickListener(e -> {
                visitorManagementService.checkoutVisitor(row.getLogId());
                Notification.show("Visitor berhasil checkout.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                mainView.refreshAllData();
            });
            return checkoutBtn;
        })).setHeader("Aksi");

        activeGrid.setAllRowsVisible(true);

        add(activeHeaderLayout, activeGrid, pagination.getLayout());
    }

    @Override
    protected Span createRiskBadge(VisitorLogResponse logResponse) {
        RiskLevel risk = logResponse.getRiskLevel() != null ? logResponse.getRiskLevel() : RiskLevel.GREEN;
        Span badge = new Span(risk.name());
        
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "6px")
            .set("font-weight", "700")
            .set("font-size", "11px")
            .set("letter-spacing", "0.5px");

        if (risk == RiskLevel.RED) {
            badge.getStyle()
                .set("background-color", "rgba(239, 68, 68, 0.15)")
                .set("color", "#ef4444")
                .set("border", "1px solid rgba(239, 68, 68, 0.3)");
        } else if (risk == RiskLevel.YELLOW) {
            badge.getStyle()
                .set("background-color", "rgba(245, 158, 11, 0.15)")
                .set("color", "#f59e0b")
                .set("border", "1px solid rgba(245, 158, 11, 0.3)");
        } else {
            badge.getStyle()
                .set("background-color", "rgba(16, 185, 129, 0.15)")
                .set("color", "#10b981")
                .set("border", "1px solid rgba(16, 185, 129, 0.3)");
        }
        return badge;
    }

    public void refreshActiveData() {
        List<VisitorLogResponse> activeVisitors = visitorManagementService.getActiveVisitors();
        pagination.setData(activeVisitors); // Mengirim data ke komponen paginasi
    }
}
