package com.vigi.gate.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vigi.gate.entity.RiskRule;
import com.vigi.gate.service.RiskRuleService;
import com.vigi.gate.views.component.riskrule.RiskRuleFormCard;
import com.vigi.gate.views.component.riskrule.RiskRuleTableCard;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@Route("risk-rules")
@RequiredArgsConstructor
@PermitAll
public class RiskruleView extends VerticalLayout {

    private final RiskRuleService riskRuleService;

    // Deklarasi sub-komponen UI yang dipecah
    private RiskRuleFormCard formCard;
    private RiskRuleTableCard tableCard;

    @PostConstruct
    public void init() {
        // Set Aturan Tampilan Utama Halaman
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        // Mengubah warna background utama menjadi gelap pekat sesuai tema
        getStyle().set("background-color", "#090d16");

        // HEADER
        H1 title = new H1("Risk Rule Management");
        title.getStyle().set("margin", "0").set("color", "#f3f4f6");
        Anchor backToDashboard = new Anchor("", "Kembali ke Dashboard");
        // Memberikan style hijau menyala pada tombol navigasi kembali
        backToDashboard.getStyle()
            .set("color", "#00ff66")
            .set("font-weight", "700")
            .set("text-decoration", "none")
            .set("text-shadow", "0 0 8px rgba(0, 255, 102, 0.4)");

        HorizontalLayout headerLayout = new HorizontalLayout(title, backToDashboard);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        add(headerLayout);

        // Struktur Grid Halaman
        HorizontalLayout mainContentLayout = new HorizontalLayout();
        mainContentLayout.setSizeFull();
        mainContentLayout.setSpacing(true);

        // Inisialisasi Sub-Komponen Mandiri
        formCard = new RiskRuleFormCard(riskRuleService, this);
        tableCard = new RiskRuleTableCard(riskRuleService, this);

        // Set ukuran lebar sub-komponen sesuai desain awal
        formCard.setWidth("35%");
        tableCard.setWidth("65%");

        mainContentLayout.add(formCard, tableCard);
        add(mainContentLayout);

        // Pemuatan data awal saat halaman dibuka
        refreshAllData();
    }

    // Eksekusi penyegaran data ke seluruh sub-komponen
    public void refreshAllData() {
        tableCard.refreshGridData();
    }

    // Jembatan agar tabel bisa memicu mode edit ke dalam form
    public void triggerEditMode(RiskRule rule) {
        formCard.enterEditMode(rule);
    }

    // Jembatan untuk mereset form jika item yang sedang diedit ternyata dihapus dari tabel
    public void notifyDelete(Long deletedId) {
        formCard.handleExternalDelete(deletedId);
    }
    
}
