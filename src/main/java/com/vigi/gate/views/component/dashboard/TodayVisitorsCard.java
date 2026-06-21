package com.vigi.gate.views.component.dashboard;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.MainView;
import com.vigi.gate.views.component.BaseCard;

public class TodayVisitorsCard extends BaseCard {

    private final VisitorManagementService visitorManagementService;
    MainView mainView;
    private final Grid<VisitorLogResponse> todayGrid = new Grid<>(VisitorLogResponse.class, false);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TodayVisitorsCard(VisitorManagementService visitorManagementService, MainView mainView) {
        super("");
        this.visitorManagementService = visitorManagementService;
        this.mainView = mainView;

        H3 todayHeaderTitle = new H3("Data Visitor Hari Ini");
        todayHeaderTitle.getStyle().set("margin", "0");
        Button refreshTodayBtn = new Button("Refresh", event -> refreshTodayData());
        refreshTodayBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout todayHeaderLayout = new HorizontalLayout(todayHeaderTitle, refreshTodayBtn);
        todayHeaderLayout.setWidthFull();
        todayHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        todayHeaderLayout.setAlignItems(Alignment.CENTER);

        // Konfigurasi Kolom Grid Riwayat Hari ini
        todayGrid.addColumn(VisitorLogResponse::getFullName).setHeader("Nama").setSortable(true);
        todayGrid.addColumn(VisitorLogResponse::getNik).setHeader("NIK");
        todayGrid.addColumn(row -> row.getCheckinTime() != null ? row.getCheckinTime().format(formatter) : "-").setHeader("Jam Checkin");
        todayGrid.addColumn(VisitorLogResponse::getPurpose).setHeader("Tujuan");
        todayGrid.addColumn(row -> row.getCheckoutTime() != null ? row.getCheckoutTime().format(formatter) : "-").setHeader("Jam Checkout");
        todayGrid.addColumn(new ComponentRenderer<>(this::createRiskBadge)).setHeader("Risk");
        todayGrid.addColumn(row -> row.getRiskScore() != null ? row.getRiskScore() : "-").setHeader("Skor");
        
        // Tombol Aksi Hapus data didalam Kolom Grid menggunakan Dialog Konfirmasi Vaadin
        todayGrid.addColumn(new ComponentRenderer<>(row -> {
            Button deleteBtn = new Button("Hapus");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.add(new H3("Konfirmasi Hapus Data"));
                confirmDialog.add(new Paragraph("Apakah Anda yakin ingin menghapus data visitor ini?"));

                Button confirmDelete = new Button("Hapus", ev -> {
                    visitorManagementService.deleteVisitor(row.getLogId());
                    Notification.show("Data berhasil dihapus.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    confirmDialog.close();
                    mainView.refreshAllData();
                });
                confirmDelete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

                Button cancelDelete = new Button("Batal", ev -> confirmDialog.close());
                
                HorizontalLayout dialogButtons = new HorizontalLayout(confirmDelete, cancelDelete);
                confirmDialog.add(dialogButtons);
                confirmDialog.open();
            });
            return deleteBtn;
        })).setHeader("Aksi");

        todayGrid.setAllRowsVisible(true);
        add(todayHeaderLayout, todayGrid);
    }

    public void refreshTodayData() {
        List<VisitorLogResponse> todayLogs = visitorManagementService.getTodayLogs();
        todayGrid.setItems(todayLogs);
    }
    
}
