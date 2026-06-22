package com.vigi.gate.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
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

    // Deklarasi sub-komponen UI yang sudah dipecah
    private RegistrationFormCard registrationFormCard;
    private ActiveVisitorsCard activeVisitorsCard;
    private SummaryReportCard summaryReportCard;
    private TodayVisitorsCard todayVisitorsCard;

    private Thread feederThread;

    @PostConstruct
    public void init() {
        addClassName("main-view-container");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Tambah Judul Utama Halaman
        H1 title = new H1("Vigi Gate - Digital Visitor Management");
        Anchor riskRulesLink = new Anchor("/risk-rules", "Kelola Risk Rules");
        riskRulesLink.getStyle().set("color", "#2563eb").set("font-weight", "700").set("text-decoration", "none");
        
        Anchor historyLink = new Anchor("/visitor-history", "Lihat Data Visitor 30 Hari Terakhir");
        historyLink.getStyle().set("color", "#2563eb").set("font-weight", "700").set("text-decoration", "none");

        HorizontalLayout linksLayout = new HorizontalLayout(riskRulesLink, historyLink);
        linksLayout.setSpacing(true);
        add(title, linksLayout);

        // Inisialisasi Sub-Komponen dengan melempar Service & referensi MainView
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

        // Gabungkan seluruh komponen ke layout utama MainView
        add(mainGridLayout);
        add(summaryReportCard);
        add(todayVisitorsCard);

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
