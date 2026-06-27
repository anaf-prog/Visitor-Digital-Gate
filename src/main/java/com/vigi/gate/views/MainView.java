package com.vigi.gate.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.component.dashboard.ActiveVisitorsCard;
import com.vigi.gate.views.component.dashboard.RegistrationFormCard;
import com.vigi.gate.views.component.dashboard.SummaryReportCard;
import com.vigi.gate.views.component.dashboard.TodayVisitorsCard;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Route("")
@Slf4j
@RequiredArgsConstructor
@PermitAll
public class MainView extends VerticalLayout {

    private final VisitorManagementService visitorManagementService;

    // Deklarasi sub-komponen UI yang sudah dipecah di file terpisah
    private RegistrationFormCard registrationFormCard;
    private ActiveVisitorsCard activeVisitorsCard;
    private SummaryReportCard summaryReportCard;
    private TodayVisitorsCard todayVisitorsCard;

    private Thread feederThread;

    @PostConstruct
    public void init() {
        addClassName("main-view-container");
        setWidthFull();
        setMinHeight("100vh"); 
        
        setPadding(true); 
        // Background utama dark mode menyeluruh
        getStyle().set("padding", "24px")
            .set("background-color", "#090d16");
        setSpacing(true);

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
        
        Span brandSub = new Span("Digital Visitor Management");
        brandSub.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("color", "#9ca3af")
            .set("margin-left", "12px")
            .set("border-left", "2px solid rgba(0, 255, 102, 0.3)")
            .set("padding-left", "12px");

        HorizontalLayout brandLayout = new HorizontalLayout(brandTitle, brandSub);
        brandLayout.setAlignItems(Alignment.CENTER);

        // Bagian Kanan Navbar: Menu Navigasi dengan Efek Hover melalui Atribut Style inline HTML
        Button riskRulesBtn = new Button("Kelola Risk Rules", VaadinIcon.SHIELD.create(), event -> {
            UI.getCurrent().navigate("risk-rules");
        });
        riskRulesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        // Mengubah style tombol ke glow in the dark green
        riskRulesBtn.getStyle()
            .set("background-color", "#00cc66")
            .set("color", "#090d16")
            .set("font-weight", "700")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease-in-out");
        
        // Menambahkan efek hover via JavaScript DOM listener internal Vaadin
        riskRulesBtn.getElement().addEventListener("mouseover", e -> {
            riskRulesBtn.getStyle().set("background-color", "#00ff66");
            riskRulesBtn.getStyle().set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.6)");
        });
        riskRulesBtn.getElement().addEventListener("mouseout", e -> {
            riskRulesBtn.getStyle().set("background-color", "#00cc66");
            riskRulesBtn.getStyle().remove("box-shadow");
        });

        Button historyBtn = new Button("Riwayat 30 Hari", VaadinIcon.TIME_BACKWARD.create(), event -> {
            UI.getCurrent().navigate("visitor-history");
        });
        historyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        // Mengubah style tombol ke glow in the dark green
        historyBtn.getStyle()
            .set("background-color", "#00cc66")
            .set("color", "#090d16")
            .set("font-weight", "700")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease-in-out");

        historyBtn.getElement().addEventListener("mouseover", e -> {
            historyBtn.getStyle().set("background-color", "#00ff66");
            historyBtn.getStyle().set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.6)");
        });
        historyBtn.getElement().addEventListener("mouseout", e -> {
            historyBtn.getStyle().set("background-color", "#00cc66");
            historyBtn.getStyle().remove("box-shadow");
        });

        HorizontalLayout menuLayout = new HorizontalLayout(riskRulesBtn, historyBtn);
        menuLayout.setSpacing(true);

        // Gabungkan komponen ke Navbar
        navbar.add(brandLayout, menuLayout);
        
        // Tambahkan Navbar ke layout paling atas
        add(navbar);

        // Container untuk membungkus komponen kartu di bawah navbar
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidthFull();
        contentLayout.setPadding(false); // Diubah ke false karena padding luar sudah diatur oleh MainView
        contentLayout.setSpacing(true);

        // Inisialisasi Sub-Komponen terpisah
        registrationFormCard = new RegistrationFormCard(visitorManagementService, this);
        activeVisitorsCard = new ActiveVisitorsCard(visitorManagementService, this);
        summaryReportCard = new SummaryReportCard(visitorManagementService);
        todayVisitorsCard = new TodayVisitorsCard(visitorManagementService, this);

        registrationFormCard.setWidth("35%");
        activeVisitorsCard.setWidth("65%");

        // Buat Layout Grid Bagian Atas
        HorizontalLayout mainGridLayout = new HorizontalLayout(registrationFormCard, activeVisitorsCard);
        mainGridLayout.setWidthFull();
        mainGridLayout.setSpacing(true);

        // Gabungkan komponen kartu ke dalam contentLayout
        contentLayout.add(mainGridLayout);
        contentLayout.add(summaryReportCard);
        contentLayout.add(todayVisitorsCard);
        
        // Masukkan semua konten ke MainView setelah Navbar
        add(contentLayout);

        // Pemuatan data pertama kali saat halaman dibuka
        refreshAllData();
    }

    // Eksekusi pembaruan seluruh komponen data di UI
    public void refreshAllData() {
        activeVisitorsCard.refreshActiveData();
        summaryReportCard.refreshSummaryData();
        todayVisitorsCard.refreshTodayData();
    }

    // Fitur Live Engine Auto-Refresh menggunakan VIRTUAL THREAD
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        UI ui = attachEvent.getUI();
        
        // Virtual Thread
        feederThread = Thread.ofVirtual().start(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(15000);
                    
                    // Memanggil UI access secara aman agar Vaadin mendorong pembaruan DOM ke browser via Websocket
                    ui.access(this::refreshAllData);
                }
            } catch (InterruptedException e) {
                log.debug("Background auto-refresh Virtual Thread dihentikan.");
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (feederThread != null) {
            feederThread.interrupt();
            feederThread = null;
        }
        super.onDetach(detachEvent);
    }
    
}
