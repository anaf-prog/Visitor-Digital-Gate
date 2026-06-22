package com.vigi.gate.views;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.component.visitorhistory.VisitorHistoryFilterCard;
import com.vigi.gate.views.component.visitorhistory.VisitorHistoryTableCard;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@Route("visitor-history")
@RequiredArgsConstructor
@PermitAll
public class VistorHistoryView extends VerticalLayout {

    private final VisitorManagementService visitorManagementService;

    // Sub-komponen UI yang dipecah
    private VisitorHistoryFilterCard filterCard;
    private VisitorHistoryTableCard tableCard;

    // Data Provider Tunggal yang di-share ke sub-komponen filter dan table
    private ListDataProvider<VisitorLogResponse> dataProvider = new ListDataProvider<>(new ArrayList<>());

    @PostConstruct
    public void init() {
        // Konfigurasi Dasar Layout Utama Halaman
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f7f8fc");

        // Header
        H1 title = new H1("Data Visitor 30 Hari Terakhir");
        title.getStyle().set("margin", "0");

        Anchor backToDashboard = new Anchor("", "← Kembali ke Dashboard");
        backToDashboard.getStyle()
            .set("color", "#2563eb")
            .set("font-weight", "600")
            .set("text-decoration", "none")
            .set("padding", "8px 16px")
            .set("border", "1px solid #d1d5db")
            .set("border-radius", "6px")
            .set("background", "#fff");

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create(), event -> refreshHistoryData());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.getStyle().set("background-color", "#2563eb");

        HorizontalLayout navActions = new HorizontalLayout(backToDashboard, refreshBtn);
        navActions.setSpacing(true);
        navActions.setAlignItems(Alignment.CENTER);

        HorizontalLayout headerLayout = new HorizontalLayout(title, navActions);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        add(headerLayout);

        // Inisialisasi Sub-Komponen dengan melempar parameter data provider & reference parent view
        filterCard = new VisitorHistoryFilterCard(dataProvider, this);
        tableCard = new VisitorHistoryTableCard(visitorManagementService, dataProvider, this);

        // Gabungkan seluruh komponen ke layout utama
        add(filterCard);
        add(tableCard);

        // Ambil data pertama kali dari database
        refreshHistoryData();
    }

    public void refreshHistoryData() {
        try {
            List<VisitorLogResponse> historyLogs = visitorManagementService.getLast30DaysLogs();
            
            // Perbarui item internal di dalam data provider tanpa memutuskan instansiasi object referensinya
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(historyLogs);
            dataProvider.refreshAll();
            
            // Picu sub-komponen filter untuk menerapkan ulang aturan filter dan memperbarui teks label counter data
            filterCard.applyDataFilters(); 
        } catch (Exception error) {
            Notification.show("Gagal memuat data history visitor.", 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            tableCard.updateRecordCountLabel("Error memuat data");
        }
    }

    // Mengizinkan sub-komponen filter untuk memperbarui label jumlah baris yang ada di card tabel
    public void updateTableRecordCount(String text) {
        tableCard.updateRecordCountLabel(text);
    }
    
}
