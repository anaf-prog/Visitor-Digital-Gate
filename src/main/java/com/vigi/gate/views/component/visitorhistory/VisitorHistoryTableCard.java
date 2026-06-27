package com.vigi.gate.views.component.visitorhistory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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

        // Menerapkan gaya dark mode pada Card Container utama
        this.getStyle()
            .set("background-color", "#111827")
            .set("padding", "20px")
            .set("border-radius", "12px")
            .set("border", "1px solid rgba(255, 255, 255, 0.08)")
            .set("--lumo-base-color", "#111827")          // Latar belakang tabel & header
            .set("--lumo-body-text-color", "#f3f4f6")     // Teks isi tabel
            .set("--lumo-secondary-text-color", "#9ca3af")// Teks header tabel
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.04)") // Garis baris (zebra striping)
            .set("--lumo-contrast-20pct", "rgba(255, 255, 255, 0.1)");  // Garis batas sel tabel

        H3 tableTitle = new H3("Data Visitor");
        tableTitle.getStyle()
            .set("margin", "0")
            .set("color", "#00ff66")
            .set("text-shadow", "0 0 8px rgba(0, 255, 102, 0.3)");

        recordCountLabel.getStyle().set("color", "#9ca3af").set("font-weight", "500");

        HorizontalLayout topActions = new HorizontalLayout(tableTitle, recordCountLabel);
        topActions.setWidthFull();
        topActions.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topActions.setAlignItems(Alignment.CENTER);
        add(topActions);

        // Sambungkan komponen grid tabel ke objek data provider bersama yang dikelola kelas induk
        grid.setDataProvider(dataProvider);

        grid.getStyle()
            .set("background-color", "#111827")
            .set("border", "1px solid rgba(255, 255, 255, 0.08)")
            .set("border-radius", "8px");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

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
            badge.getStyle().set("font-weight", "800");
            
            // Konfigurasi warna teks badge resiko yang adaptif di layar gelap
            if (RiskLevel.RED.equals(level)) {
                badge.getStyle().set("color", "#f87171").set("text-shadow", "0 0 6px rgba(248, 113, 113, 0.4)");
            } else if (RiskLevel.YELLOW.equals(level)) {
                badge.getStyle().set("color", "#fbbf24").set("text-shadow", "0 0 6px rgba(251, 191, 36, 0.4)");
            } else {
                badge.getStyle().set("color", "#34d399").set("text-shadow", "0 0 6px rgba(52, 211, 153, 0.4)");
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
                    .set("border-radius", "6px")
                    .set("border", "1px solid rgba(0, 255, 102, 0.3)")
                    .set("object-fit", "cover")
                    .set("cursor", "pointer")
                    .set("transition", "transform 0.2s");
                imgPreview.addClickListener(e -> openPhotoModal(res.getPhotoUrl()));
                return imgPreview;
            } else {
                Span noPhotoSpan = new Span("Tidak ada");
                noPhotoSpan.getStyle().set("color", "#6b7280").set("font-size", "12px");
                return noPhotoSpan;
            }
        })).setHeader("Foto").setAutoWidth(true);

        // Render Kolom Tombol Aksi Hapus
        grid.addColumn(new ComponentRenderer<>(res -> {
            Button deleteBtn = new Button("Hapus");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
 
            deleteBtn.getStyle()
                .set("background-color", "rgba(239, 68, 68, 0.15)") // Warna dasar merah redup transparan
                .set("color", "#f87171")                             // Warna teks merah soft
                .set("border", "1px solid rgba(239, 68, 68, 0.4)")  // Border merah tipis
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out");          // Transisi animasi menyeluruh
                
            // Menambahkan efek hover kustom pada tombol Hapus
            deleteBtn.getElement().addEventListener("mouseover", e -> {
                deleteBtn.getStyle()
                    .set("background-color", "#ef4444")
                    .set("color", "#ffffff")
                    .set("box-shadow", "0 0 10px rgba(239, 68, 68, 0.45)");
            });
            
            deleteBtn.getElement().addEventListener("mouseout", e -> {
                deleteBtn.getStyle()
                    .set("background-color", "rgba(239, 68, 68, 0.15)")
                    .set("color", "#f87171")
                    .remove("box-shadow");
            });

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

        photoDialog.getElement().executeJs(
            "this.$.overlay.$.overlay.style.backgroundColor = '#1e293b';" +
            "this.$.overlay.$.overlay.style.color = '#f1f5f9';"
        );

        H3 dialogTitle = new H3("Foto Visitor");
        dialogTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "20px")
            .set("color", "#00ff66");

        VerticalLayout photoWrapper = new VerticalLayout();
        photoWrapper.setAlignItems(Alignment.CENTER);
        photoWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        photoWrapper.setPadding(false);

        Image fullImage = new Image(photoUrl, "Foto Visitor Besar");
        fullImage.getStyle()
            .set("max-width", "100%")
            .set("max-height", "400px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.5)");
        photoWrapper.add(fullImage);

        Button closeBtn = new Button("Tutup", event -> photoDialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle().set("color", "#9ca3af");

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

        confirmDialog.getElement().executeJs(
            "this.$.overlay.$.overlay.style.backgroundColor = '#1e293b';" +
            "this.$.overlay.$.overlay.style.color = '#f1f5f9';"
        );

        H3 dialogTitle = new H3("Konfirmasi Hapus Data");
        dialogTitle.getStyle().set("margin-top", "0").set("color", "#f87171");

        Paragraph bodyText = new Paragraph("Apakah Anda yakin ingin menghapus data visitor " + res.getFullName() + "?");
        bodyText.getStyle().set("color", "#9ca3af"); 

        Paragraph warningText = new Paragraph("Tindakan ini tidak dapat dibatalkan.");
        warningText.getStyle().set("color", "#f87171").set("font-weight", "700").set("font-size", "13px");

        Button confirmBtn = new Button("Hapus", event -> {
            try {
                visitorManagementService.deleteVisitor(res.getLogId());
                Notification.show("Data visitor berhasil dihapus.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                
                vistorHistoryView.refreshHistoryData();
            } catch (Exception ex) {
                Notification.show("Gagal menghapus data visitor.", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                confirmDialog.close();
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        confirmBtn.getStyle().set("transition", "background-color 0.2s");
        confirmBtn.getElement().addEventListener("mouseover", e -> confirmBtn.getStyle().set("background-color", "#dc2626"));
        confirmBtn.getElement().addEventListener("mouseout", e -> confirmBtn.getStyle().set("background-color", ""));

        Button cancelBtn = new Button("Batal", event -> confirmDialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.getStyle().set("color", "#9ca3af").set("transition", "color 0.2s");
        cancelBtn.getElement().addEventListener("mouseover", e -> cancelBtn.getStyle().set("color", "#ffffff"));
        cancelBtn.getElement().addEventListener("mouseout", e -> cancelBtn.getStyle().set("color", "#9ca3af"));

        HorizontalLayout footerLayout = new HorizontalLayout(cancelBtn, confirmBtn);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(JustifyContentMode.END);
        footerLayout.getStyle().set("margin-top", "20px");

        confirmDialog.add(dialogTitle, bodyText, warningText, footerLayout);
        confirmDialog.open();
    }
    
}
