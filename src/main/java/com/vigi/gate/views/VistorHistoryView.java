package com.vigi.gate.views;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
        // Mengubah background view utama ke dark theme
        getStyle().set("padding", "24px")
            .set("background-color", "#090d16");

        // --- PEMBUATAN HEADER / NAVBAR MODERN ---
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.setWidthFull();
        navbar.setPadding(false);
        navbar.setSpacing(true);
        navbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navbar.setAlignItems(Alignment.CENTER);
        
        navbar.getStyle()
            .set("background-color", "#111827")
            .set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.05)")
            .set("border", "1px solid rgba(0, 255, 102, 0.15)")
            .set("border-radius", "12px")
            .set("padding", "16px 24px");

        // Bagian Kiri Navbar: Logo / Judul Utama
        H2 brandTitle = new H2("Vigi Gate");
        brandTitle.getStyle()
            .set("margin", "0")
            .set("font-size", "24px")
            .set("font-weight", "900")
            .set("color", "#00ff66")
            .set("text-shadow", "0 0 10px rgba(0, 255, 102, 0.4)")
            .set("letter-spacing", "0.5px");
        
        Span brandSub = new Span("Data Visitor 30 Hari Terakhir");
        brandSub.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("color", "#9ca3af")
            .set("margin-left", "12px")
            .set("border-left", "2px solid rgba(0, 255, 102, 0.3)")
            .set("padding-left", "12px");

        HorizontalLayout brandLayout = new HorizontalLayout(brandTitle, brandSub);
        brandLayout.setAlignItems(Alignment.CENTER);

        // Bagian Kanan Navbar: Menu Navigasi (Kembali ke Dashboard & Kelola Risk Rules)
        Button backToDashboardBtn = new Button("Kembali ke Dashboard", VaadinIcon.HOME.create(), event -> {
            UI.getCurrent().navigate("");
        });
        backToDashboardBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backToDashboardBtn.getStyle()
            .set("background-color", "#00cc66")
            .set("color", "#090d16")
            .set("font-weight", "700")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease-in-out");
        
        backToDashboardBtn.getElement().addEventListener("mouseover", e -> {
            backToDashboardBtn.getStyle().set("background-color", "#00ff66");
            backToDashboardBtn.getStyle().set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.6)");
        });
        backToDashboardBtn.getElement().addEventListener("mouseout", e -> {
            backToDashboardBtn.getStyle().set("background-color", "#00cc66");
            backToDashboardBtn.getStyle().remove("box-shadow");
        });

        Button riskRulesBtn = new Button("Kelola Risk Rules", VaadinIcon.SHIELD.create(), event -> {
            UI.getCurrent().navigate("risk-rules");
        });
        riskRulesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        riskRulesBtn.getStyle()
            .set("background-color", "#00cc66")
            .set("color", "#090d16")
            .set("font-weight", "700")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease-in-out");

        riskRulesBtn.getElement().addEventListener("mouseover", e -> {
            riskRulesBtn.getStyle().set("background-color", "#00ff66");
            riskRulesBtn.getStyle().set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.6)");
        });
        riskRulesBtn.getElement().addEventListener("mouseout", e -> {
            riskRulesBtn.getStyle().set("background-color", "#00cc66");
            riskRulesBtn.getStyle().remove("box-shadow");
        });

        HorizontalLayout menuLayout = new HorizontalLayout(backToDashboardBtn, riskRulesBtn);
        menuLayout.setSpacing(true);

        // Gabungkan komponen ke Navbar
        navbar.add(brandLayout, menuLayout);
        
        // Tambahkan Navbar ke layout paling atas
        add(navbar);

        // Inisialisasi Sub-Komponen dengan melempar parameter data provider & reference parent view
        filterCard = new VisitorHistoryFilterCard(dataProvider, this);
        tableCard = new VisitorHistoryTableCard(visitorManagementService, dataProvider, this);

        // Gabungkan seluruh komponen ke layout utama di bawah navbar
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
