package com.vigi.gate.views.component.dashboard;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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

public class TodayVisitorsCard extends BaseCard {

    private final VisitorManagementService visitorManagementService;
    MainView mainView;
    private final Grid<VisitorLogResponse> todayGrid = new Grid<>(VisitorLogResponse.class, false);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TodayVisitorsCard(VisitorManagementService visitorManagementService, MainView mainView) {
        super("");
        this.visitorManagementService = visitorManagementService;
        this.mainView = mainView;

        // Mengaktifkan tema gelap untuk komponen container kartu ini
        getElement().setAttribute("theme", "dark");

        // Styling Card Container utama agar serasi dengan bagian registrasi dan realtime log
        getStyle()
            .set("background-color", "#111827")
            .set("border", "1px solid rgba(0, 255, 102, 0.15)")
            .set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.05)")
            .set("border-radius", "12px")
            .set("padding", "20px")
            .set("color", "#f3f4f6");

        H3 todayHeaderTitle = new H3("Data Visitor Hari Ini");
        todayHeaderTitle.getStyle().set("margin", "0").set("color", "#f3f4f6");
        
        Button refreshTodayBtn = new Button("Refresh", event -> refreshTodayData());
        refreshTodayBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        refreshTodayBtn.getStyle()
            .set("background-color", "rgba(0, 255, 102, 0.1)")
            .set("color", "#00ff66")
            .set("border", "1px solid rgba(0, 255, 102, 0.3)")
            .set("font-weight", "600")
            .set("cursor", "pointer");

        HorizontalLayout todayHeaderLayout = new HorizontalLayout(todayHeaderTitle, refreshTodayBtn);
        todayHeaderLayout.setWidthFull();
        todayHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        todayHeaderLayout.setAlignItems(Alignment.CENTER);
        todayHeaderLayout.getStyle().set("margin-bottom", "16px");

        // Kustomisasi Styling Grid agar menyesuaikan dengan Dark Mode
        todayGrid.getElement().setAttribute("theme", "dark");
        todayGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        
        todayGrid.getStyle()
            .set("background-color", "#111827")
            .set("--lumo-base-color", "#111827")               // Latar belakang utama Grid
            .set("--lumo-body-text-color", "#f3f4f6")           // Warna teks data baris
            .set("--lumo-header-text-color", "#00ff66")         // Warna teks header kolom (Glow)
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.03)") // Efek belang (row stripes)
            .set("--lumo-contrast-20pct", "rgba(0, 255, 102, 0.15)")   // Garis pemisah baris tabel
            .set("border", "1px solid rgba(0, 255, 102, 0.1)")
            .set("border-radius", "8px");

        // Konfigurasi Kolom Grid Riwayat Hari ini
        todayGrid.addColumn(VisitorLogResponse::getFullName).setHeader("Nama").setSortable(true);
        todayGrid.addColumn(VisitorLogResponse::getNik).setHeader("NIK");
        todayGrid.addColumn(row -> row.getCheckinTime() != null ? row.getCheckinTime().format(formatter) : "-").setHeader("Jam Checkin");
        todayGrid.addColumn(VisitorLogResponse::getPurpose).setHeader("Tujuan");
        todayGrid.addColumn(row -> row.getCheckoutTime() != null ? row.getCheckoutTime().format(formatter) : "-").setHeader("Jam Checkout");
        todayGrid.addColumn(new ComponentRenderer<>(this::createRiskBadge)).setHeader("Risk");
        todayGrid.addColumn(row -> row.getRiskScore() != null ? row.getRiskScore() : "-").setHeader("Skor");
        
        // Tombol Aksi Hapus data didalam Kolom Grid menggunakan Dialog Konfirmasi Vaadin bertema gelap
        todayGrid.addColumn(new ComponentRenderer<>(row -> {
            Button deleteBtn = new Button("Hapus");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            
            // Memberikan aksen warna merah transparan yang menyatu dengan tema gelap
            deleteBtn.getStyle()
                .set("background-color", "rgba(239, 68, 68, 0.15)")
                .set("color", "#ef4444")
                .set("border", "1px solid rgba(239, 68, 68, 0.3)")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out");

            deleteBtn.getElement().addEventListener("mouseover", e -> {
                deleteBtn.getStyle().set("background-color", "#ef4444");
                deleteBtn.getStyle().set("color", "#ffffff");
                deleteBtn.getStyle().set("box-shadow", "0 0 10px rgba(239, 68, 68, 0.5)");
            });
            deleteBtn.getElement().addEventListener("mouseout", e -> {
                deleteBtn.getStyle().set("background-color", "rgba(239, 68, 68, 0.15)");
                deleteBtn.getStyle().set("color", "#ef4444");
                deleteBtn.getStyle().remove("box-shadow");
            });

            deleteBtn.addClickListener(e -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.getElement().setAttribute("theme", "dark"); // Menyesuaikan tema gelap untuk dialog
                
                H3 dialogTitle = new H3("Konfirmasi Hapus Data");
                dialogTitle.getStyle().set("color", "#f3f4f6").set("margin-top", "0");
                
                Paragraph dialogMessage = new Paragraph("Apakah Anda yakin ingin menghapus data visitor ini?");
                dialogMessage.getStyle().set("color", "#9ca3af");

                confirmDialog.add(dialogTitle, dialogMessage);

                Button confirmDelete = new Button("Hapus", ev -> {
                    visitorManagementService.deleteVisitor(row.getLogId());
                    Notification.show("Data berhasil dihapus.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    confirmDialog.close();
                    mainView.refreshAllData();
                });
                confirmDelete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

                Button cancelDelete = new Button("Batal", ev -> confirmDialog.close());
                cancelDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                cancelDelete.getStyle().set("color", "#9ca3af");
                
                HorizontalLayout dialogButtons = new HorizontalLayout(confirmDelete, cancelDelete);
                dialogButtons.setSpacing(true);
                dialogButtons.getStyle().set("margin-top", "20px");
                
                confirmDialog.add(dialogButtons);
                confirmDialog.open();
            });
            return deleteBtn;
        })).setHeader("Aksi");

        todayGrid.setAllRowsVisible(true);
        add(todayHeaderLayout, todayGrid);
    }

    /**
     * Override kustom badge risiko dengan visibilitas protected agar sesuai dengan parent class (BaseCard).
     * Menggunakan tipe data enum RiskLevel.
     */
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
            // GREEN / LOW
            badge.getStyle()
                .set("background-color", "rgba(16, 185, 129, 0.15)")
                .set("color", "#10b981")
                .set("border", "1px solid rgba(16, 185, 129, 0.3)");
        }
        return badge;
    }

    public void refreshTodayData() {
        List<VisitorLogResponse> todayLogs = visitorManagementService.getTodayLogs();
        todayGrid.setItems(todayLogs);
    }
    
}
