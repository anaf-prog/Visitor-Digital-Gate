package com.vigi.gate.views.component.visitorhistory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.VistorHistoryView;
import com.vigi.gate.views.component.BaseCard;
import com.vigi.gate.views.component.GridPagination;

public class VisitorHistoryTableCard extends BaseCard {

    final VisitorManagementService visitorManagementService;
    final VistorHistoryView vistorHistoryView;
    
    private final Grid<VisitorLogResponse> grid = new Grid<>();
    private final Span recordCountLabel = new Span("Memuat data...");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
    
    // Deklarasi object GridPagination kustom
    private final GridPagination<VisitorLogResponse> pagination;

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
            .set("--lumo-contrast-20pct", "rgba(255, 255, 255, 0.1)")  // Garis batas sel tabel
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("overflow", "hidden");

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

        grid.getStyle()
            .set("background-color", "#111827")
            .set("border", "1px solid rgba(255, 255, 255, 0.08)")
            .set("border-radius", "8px")
            .set("scrollbar-color", "#1e293b #111827")
            .set("scrollbar-width", "thin");

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
                
                imgPreview.addClickListener(e -> new VisitorPhotoDialog(res.getPhotoUrl()).open());
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
                .set("background-color", "rgba(239, 68, 68, 0.15)")
                .set("color", "#f87171")
                .set("border", "1px solid rgba(239, 68, 68, 0.4)")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out");
                
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

            deleteBtn.addClickListener(e -> new VisitorDeleteConfirmDialog(res, visitorManagementService, vistorHistoryView).open());
            
            HorizontalLayout layout = new HorizontalLayout(deleteBtn);
            layout.setJustifyContentMode(JustifyContentMode.CENTER);
            return layout;
        })).setHeader("Aksi").setAutoWidth(true);

        grid.setPageSize(15);
        grid.setHeight("550px");
        
        // Inisialisasi komponen paginasi
        pagination = new GridPagination<>(grid, 10);
        
        // Ambil data jika memang dari awal sudah ada isinya
        pagination.setData(new ArrayList<>(dataProvider.getItems()));

        // Pasang listener agar otomatis mendeteksi ketika dataProvider diisi/diubah oleh View induk
        dataProvider.addDataProviderListener(event -> {
            // Mengambil item yang lolos dari filter dataProvider saat ini
            ArrayList<VisitorLogResponse> filteredItems = dataProvider.getItems().stream()
                    .filter(item -> dataProvider.getFilter() == null || dataProvider.getFilter().test(item))
                    .collect(Collectors.toCollection(ArrayList::new));
            
            pagination.setData(filteredItems);
        });

        // Masukkan komponen grid dan layout kontrol paginasi ke view
        add(grid, pagination.getLayout());
    }
    
    public void refreshPaginationData(ArrayList<VisitorLogResponse> updatedList) {
        if (pagination != null) {
            pagination.setData(updatedList);
        }
    }

    public void updateRecordCountLabel(String text) {
        recordCountLabel.setText(text);
    }

    private String formatDateTimeString(LocalDateTime localDateTime) {
        if (localDateTime == null) return "-";
        return localDateTime.format(dateTimeFormatter);
    }
    
}
