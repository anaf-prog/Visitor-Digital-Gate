package com.vigi.gate.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        // 1. Set Aturan Tampilan Utama Halaman (Gunakan setHeightFull bukan setSizeFull agar terkontrol)
        setHeightFull();
        setWidthFull();
        setPadding(false); // Matikan padding bawaan Vaadin agar tidak tabrakan dengan CSS kustom
        setSpacing(true);
        
        // Mengubah warna background utama menjadi gelap pekat sesuai tema diseluruh area viewport
        getStyle()
            .set("padding", "24px")
            .set("background-color", "#090d16")
            .set("box-sizing", "border-box")
            .set("overflow", "hidden"); // Mencegah scrollbar utama browser muncul

        // --- PEMBUATAN HEADER / NAVBAR MODERN (Sama seperti Dashboard) ---
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
        
        Span brandSub = new Span("Risk Rule Management");
        brandSub.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("color", "#9ca3af")
            .set("margin-left", "12px")
            .set("border-left", "2px solid rgba(0, 255, 102, 0.3)")
            .set("padding-left", "12px");

        HorizontalLayout brandLayout = new HorizontalLayout(brandTitle, brandSub);
        brandLayout.setAlignItems(Alignment.CENTER);

        // Bagian Kanan Navbar: Menu Navigasi dengan Efek Hover
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

        Button historyBtn = new Button("Riwayat 30 Hari", VaadinIcon.TIME_BACKWARD.create(), event -> {
            UI.getCurrent().navigate("visitor-history");
        });
        historyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
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

        HorizontalLayout menuLayout = new HorizontalLayout(backToDashboardBtn, historyBtn);
        menuLayout.setSpacing(true);

        // Gabungkan komponen ke Navbar
        navbar.add(brandLayout, menuLayout);
        
        // Tambahkan Navbar ke layout paling atas
        add(navbar);

        // Struktur Grid Halaman Konten di bawah navbar
        HorizontalLayout mainContentLayout = new HorizontalLayout();
        mainContentLayout.setWidthFull();
        // Menggunakan setFlexGrow agar layout konten otomatis mengisi sisa area tinggi layar tanpa merusak proporsi
        setFlexGrow(1.0, mainContentLayout); 
        mainContentLayout.setSpacing(true);
        
        // Memaksa background kontainer konten ikut gelap agar sela-sela putih hilang total
        mainContentLayout.getStyle()
            .set("background-color", "#090d16")
            .set("min-height", "0"); // Penting di CSS Flexbox untuk mencegah layout anak memaksa tinggi melebihi parent

        // Inisialisasi Sub-Komponen Mandiri
        formCard = new RiskRuleFormCard(riskRuleService, this);
        tableCard = new RiskRuleTableCard(riskRuleService, this);

        // Set ukuran lebar sub-komponen sesuai desain awal
        formCard.setWidth("35%");
        tableCard.setWidth("65%");
        
        // Mengunci tinggi card agar menyesuaikan diri dengan rapi di dalam container utama
        formCard.setHeightFull();
        tableCard.setHeightFull();

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
