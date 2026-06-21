package com.vigi.gate.views.component.visitorhistory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.VistorHistoryView;
import com.vigi.gate.views.component.BaseCard;

public class VisitorHistoryTableCard extends BaseCard {

    private final VisitorManagementService visitorManagementService;
    private final VistorHistoryView vistorHistoryView;
    
    private final Grid<VisitorLogResponse> grid = new Grid<>();
    private final Span recordCountLabel = new Span("Memuat data...");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    public VisitorHistoryTableCard(VisitorManagementService visitorManagementService, 
                                   ListDataProvider<VisitorLogResponse> dataProvider, 
                                   VistorHistoryView vistorHistoryView) {
        super("");
        this.visitorManagementService = visitorManagementService;
        this.vistorHistoryView = vistorHistoryView;

        H3 tableTitle = new H3("Data Visitor");
        tableTitle.getStyle().set("margin", "0");
        recordCountLabel.getStyle().set("color", "#6b7280").set("font-weight", "500");

        HorizontalLayout topActions = new HorizontalLayout(tableTitle, recordCountLabel);
        topActions.setWidthFull();
        topActions.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topActions.setAlignItems(Alignment.CENTER);
        add(topActions);

        // Sambungkan komponen grid tabel ke objek data provider bersama yang dikelola kelas induk
        grid.setDataProvider(dataProvider);

        // Konfigurasi Kolom Grid
        grid.addColumn(VisitorLogResponse::getFullName).setHeader("Nama").setSortable(true).setAutoWidth(true);
        grid.addColumn(VisitorLogResponse::getNik).setHeader("NIK").setAutoWidth(true);
        
        grid.addColumn(res -> formatDateTimeString(res.getCheckinTime())).setHeader("Jam Checkin").setSortable(true).setAutoWidth(true);
        grid.addColumn(VisitorLogResponse::getPurpose).setHeader("Tujuan").setAutoWidth(true);
        grid.addColumn(res -> formatDateTimeString(res.getCheckoutTime())).setHeader("Jam Checkout").setAutoWidth(true);
        
        // Render Kolom Tingkat Risiko (Risk Level Badge)
        grid.addColumn(new ComponentRenderer<Span, VisitorLogResponse>(res -> {
            RiskLevel level = res.getRiskLevel() != null ? res.getRiskLevel() : RiskLevel.GREEN;
            Span badge = new Span(level.name());
            badge.getStyle().set("font-weight", "700");
            
            if (RiskLevel.RED.equals(level)) {
                badge.getStyle().set("color", "#dc2626");
            } else if (RiskLevel.YELLOW.equals(level)) {
                badge.getStyle().set("color", "#d97706");
            } else {
                badge.getStyle().set("color", "#166534");
            }
            return badge;
        })).setHeader("Risk").setSortable(true).setAutoWidth(true);

        grid.addColumn(res -> res.getRiskScore() != null ? res.getRiskScore().toString() : "-")
            .setHeader("Skor").setSortable(true).setAutoWidth(true);

        // Render Kolom Preview Foto Cloudinary
        grid.addColumn(new ComponentRenderer<com.vaadin.flow.component.Component, VisitorLogResponse>(res -> {
            if (res.getPhotoUrl() != null && !res.getPhotoUrl().isEmpty()) {
                Image imgPreview = new Image(res.getPhotoUrl(), "Foto");
                imgPreview.setWidth("40px");
                imgPreview.setHeight("40px");
                imgPreview.getStyle()
                    .set("border-radius", "4px")
                    .set("object-fit", "cover")
                    .set("cursor", "pointer")
                    .set("transition", "transform 0.2s");
                imgPreview.addClickListener(e -> openPhotoModal(res.getPhotoUrl()));
                return imgPreview;
            } else {
                Span noPhotoSpan = new Span("Tidak ada");
                noPhotoSpan.getStyle().set("color", "#9ca3af").set("font-size", "12px");
                return noPhotoSpan;
            }
        })).setHeader("Foto").setAutoWidth(true);

        // Render Kolom Tombol Aksi Hapus
        grid.addColumn(new ComponentRenderer<>(res -> {
            Button deleteBtn = new Button("Hapus");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.getStyle().set("background-color", "#dc2626").set("color", "white");
            deleteBtn.addClickListener(e -> showDeleteConfirmation(res));
            
            HorizontalLayout layout = new HorizontalLayout(deleteBtn);
            layout.setJustifyContentMode(JustifyContentMode.CENTER);
            return layout;
        })).setHeader("Aksi").setAutoWidth(true);

        grid.setPageSize(15);
        grid.setAllRowsVisible(true);
        add(grid);
    }

    public void updateRecordCountLabel(String text) {
        recordCountLabel.setText(text);
    }

    private String formatDateTimeString(LocalDateTime localDateTime) {
        if (localDateTime == null) return "-";
        return localDateTime.format(dateTimeFormatter);
    }

    private void openPhotoModal(String photoUrl) {
        Dialog photoDialog = new Dialog();
        photoDialog.setCloseOnEsc(true);
        photoDialog.setCloseOnOutsideClick(true);

        H3 dialogTitle = new H3("Foto Visitor");
        dialogTitle.getStyle().set("margin-top", "0");

        VerticalLayout photoWrapper = new VerticalLayout();
        photoWrapper.setAlignItems(Alignment.CENTER);
        photoWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        photoWrapper.setPadding(false);

        Image fullImage = new Image(photoUrl, "Foto Visitor Besar");
        fullImage.getStyle()
            .set("max-width", "100%")
            .set("max-height", "400px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");
        photoWrapper.add(fullImage);

        Button closeBtn = new Button("Tutup", event -> photoDialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footer = new HorizontalLayout(closeBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.END);
        footer.getStyle().set("margin-top", "16px");

        photoDialog.add(dialogTitle, photoWrapper, footer);
        photoDialog.open();
    }

    private void showDeleteConfirmation(VisitorLogResponse res) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        H3 dialogTitle = new H3("Konfirmasi Hapus Data");
        dialogTitle.getStyle().set("margin-top", "0");

        Paragraph bodyText = new Paragraph("Apakah Anda yakin ingin menghapus data visitor " + res.getFullName() + "?");
        Paragraph warningText = new Paragraph("Tindakan ini tidak dapat dibatalkan.");
        warningText.getStyle().set("color", "#dc2626").set("font-weight", "700").set("font-size", "13px");

        Button confirmBtn = new Button("Hapus", event -> {
            try {
                visitorManagementService.deleteVisitor(res.getLogId());
                Notification.show("Data visitor berhasil dihapus.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                
                // Minta view induk untuk memuat ulang data terbaru dari service database
                vistorHistoryView.refreshHistoryData();
            } catch (Exception ex) {
                Notification.show("Gagal menghapus data visitor.", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                confirmDialog.close();
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Batal", event -> confirmDialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footerLayout = new HorizontalLayout(cancelBtn, confirmBtn);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(JustifyContentMode.END);
        footerLayout.getStyle().set("margin-top", "20px");

        confirmDialog.add(dialogTitle, bodyText, warningText, footerLayout);
        confirmDialog.open();
    }
    
}
