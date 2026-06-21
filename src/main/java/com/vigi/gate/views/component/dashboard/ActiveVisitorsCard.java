package com.vigi.gate.views.component.dashboard;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.MainView;
import com.vigi.gate.views.component.BaseCard;

public class ActiveVisitorsCard extends BaseCard {
    
    private final VisitorManagementService visitorManagementService;
    MainView mainView;
    private final Grid<VisitorLogResponse> activeGrid = new Grid<>(VisitorLogResponse.class, false);

    public ActiveVisitorsCard(VisitorManagementService visitorManagementService, MainView mainView) {
        super(""); // Memanggil konstruktor BaseCard
        this.visitorManagementService = visitorManagementService;
        this.mainView = mainView;

        H3 activeHeaderTitle = new H3("Realtime Log - Active Visitors");
        activeHeaderTitle.getStyle().set("margin", "0");
        Button refreshActiveBtn = new Button("Refresh", event -> refreshActiveData());
        refreshActiveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        
        HorizontalLayout activeHeaderLayout = new HorizontalLayout(activeHeaderTitle, refreshActiveBtn);
        activeHeaderLayout.setWidthFull();
        activeHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        activeHeaderLayout.setAlignItems(Alignment.CENTER);

        // Konfigurasi Kolom Grid Aktif
        activeGrid.addColumn(VisitorLogResponse::getFullName).setHeader("Nama").setSortable(true);
        activeGrid.addColumn(VisitorLogResponse::getNik).setHeader("NIK");
        activeGrid.addColumn(VisitorLogResponse::getPurpose).setHeader("Tujuan");
        activeGrid.addColumn(new ComponentRenderer<com.vaadin.flow.component.html.Span, VisitorLogResponse>(this::createRiskBadge)).setHeader("Risk");
        activeGrid.addColumn(row -> row.getRiskScore() != null ? row.getRiskScore() : "-").setHeader("Skor");
        
        // Tombol Aksi Checkout didalam Kolom Grid
        activeGrid.addColumn(new ComponentRenderer<>(row -> {
            Button checkoutBtn = new Button("Checkout");
            checkoutBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            checkoutBtn.addClickListener(e -> {
                visitorManagementService.checkoutVisitor(row.getLogId());
                Notification.show("Visitor berhasil checkout.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                mainView.refreshAllData();
            });
            return checkoutBtn;
        })).setHeader("Aksi");

        activeGrid.setAllRowsVisible(true);
        add(activeHeaderLayout, activeGrid);
    }

    public void refreshActiveData() {
        List<VisitorLogResponse> activeVisitors = visitorManagementService.getActiveVisitors();
        activeGrid.setItems(activeVisitors);
    }
}
