package com.vigi.gate.views.component.riskrule;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vigi.gate.entity.RiskRule;
import com.vigi.gate.service.RiskRuleService;
import com.vigi.gate.views.RiskruleView;
import com.vigi.gate.views.component.BaseCard;

public class RiskRuleTableCard extends BaseCard{

    private final RiskRuleService riskRuleService;
    private final RiskruleView riskruleView;
    private final Grid<RiskRule> grid = new Grid<>(RiskRule.class, false);
    
    // Komponen Judul Card Dinamis agar seragam dengan form card
    private final H3 tableTitle = new H3("Daftar Risk Rule");

    public RiskRuleTableCard(RiskRuleService riskRuleService, RiskruleView riskruleView) {
        super(""); // Kosongkan karena memakai title dinamis `tableTitle` di bawah
        this.riskRuleService = riskRuleService;
        this.riskruleView = riskruleView;

        // Mengatur gaya visual Judul agar berwarna hijau berpendar
        tableTitle.getStyle().set("margin-top", "0").set("color", "#00ff66").set("text-shadow", "0 0 8px rgba(0,255,102,0.3)");
        add(tableTitle);

        // Mengatur gaya visual Grid agar sesuai dengan tema gelap
        grid.getStyle()
            .set("background-color", "#0f172a")               // Latar belakang tabel (Slate 900)
            .set("--lumo-base-color", "#0f172a")              // Latar belakang baris tabel
            .set("--lumo-body-text-color", "#e2e8f0")         // Warna teks isi tabel (Slate 200)
            .set("--lumo-header-text-color", "#00ff66")       // Warna teks header tabel (Glow Green)
            .set("--lumo-contrast-5pct", "rgba(255, 255, 255, 0.03)")  // Latar belakang baris belang (zebra)
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.08)") // Efek hover pada baris
            .set("--lumo-contrast-20pct", "rgba(255, 255, 255, 0.15)") // Warna garis pembatas (border)
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")       // Garis batas luar tabel
            .set("border-radius", "8px");

        grid.addColumn(RiskRule::getRuleName).setHeader("Rule").setSortable(true);
        grid.addColumn(rule -> rule.getConditionType() + ": " + rule.getConditionValue()).setHeader("Kondisi");
        grid.addColumn(RiskRule::getRiskLevel).setHeader("Risk");
        grid.addColumn(RiskRule::getScoreContribution).setHeader("Score");
        
        grid.addColumn(new ComponentRenderer<Span, RiskRule>(rule -> {
            Span statusBadge = new Span(rule.isActive() ? "ACTIVE" : "INACTIVE");
            // Menyesuaikan warna badge agar lebih ramah dark mode dan berpendar cerah
            if (rule.isActive()) {
                statusBadge.getStyle()
                    .set("color", "#34d399")
                    .set("font-weight", "800")
                    .set("text-shadow", "0 0 6px rgba(52, 211, 153, 0.4)");
            } else {
                statusBadge.getStyle()
                    .set("color", "#9ca3af")
                    .set("font-weight", "700");
            }
            return statusBadge;
        })).setHeader("Status").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(rule -> {
            Button editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.getStyle()
                .set("background-color", "#d97706")
                .set("color", "#fff")
                .set("font-weight", "600")
                .set("transition", "background-color 0.2s ease");
            editBtn.addClickListener(e -> riskruleView.triggerEditMode(rule));

            // Menambahkan efek hover kustom pada tombol Edit via JavaScript Listener
            editBtn.getElement().addEventListener("mouseover", e -> editBtn.getStyle().set("background-color", "#f59e0b"));
            editBtn.getElement().addEventListener("mouseout", e -> editBtn.getStyle().set("background-color", "#d97706"));

            Button deleteBtn = new Button("Hapus");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.getStyle()
                .set("background-color", "rgba(239, 68, 68, 0.15)") // Warna dasar merah redup transparan
                .set("color", "#f87171")                            // Warna teks merah soft
                .set("border", "1px solid rgba(239, 68, 68, 0.4)") // Border merah tipis
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out");
            deleteBtn.addClickListener(e -> showDeleteConfirmation(rule));

            // Menambahkan efek hover kustom pada tombol Hapus agar menyala merah solid
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

            HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
            actions.setSpacing(true);
            return actions;
        })).setHeader("Aksi").setAutoWidth(true);

        grid.setAllRowsVisible(true);
        add(grid);
    }

    public void refreshGridData() {
        List<RiskRule> rules = riskRuleService.getAllRules();
        grid.setItems(rules);
    }

    private void showDeleteConfirmation(RiskRule rule) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        // Menyesuaikan style dialog agar konsisten dengan tema gelap Slate melalui pemanggilan elemen internal overlay
        confirmDialog.getElement().getStyle()
            .set("background-color", "#1e293b")
            .set("color", "#f1f5f9");
            
        // Menargetkan bagian overlay part content agar warna dasar putih bawaan Vaadin berubah total jadi gelap
        confirmDialog.getElement().executeJs(
            "this.$.overlay.$.overlay.style.backgroundColor = '#1e293b';" +
            "this.$.overlay.$.overlay.style.color = '#f1f5f9';"
        );

        H3 dialogTitle = new H3("Konfirmasi Hapus");
        dialogTitle.getStyle().set("margin-top", "0").set("color", "#ffffff");
        
        Paragraph textInfo = new Paragraph();
        textInfo.getStyle().set("color", "#cbd5e1");
        textInfo.add("Apakah Anda yakin ingin menghapus risk rule ");
        Span ruleNameHighlight = new Span(rule.getRuleName());
        ruleNameHighlight.getStyle().set("font-weight", "700").set("color", "#00ff66");
        textInfo.add(ruleNameHighlight);
        textInfo.add("?");

        Paragraph warningText = new Paragraph("Tindakan ini tidak dapat dibatalkan.");
        warningText.getStyle().set("color", "#f87171").set("font-weight", "700").set("font-size", "13px");

        Button confirmDeleteButton = new Button("Hapus", event -> {
            try {
                riskRuleService.deleteRule(rule.getId());
                Notification.show("Risk rule berhasil dihapus.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();

                riskruleView.notifyDelete(rule.getId());
                riskruleView.refreshAllData();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                confirmDialog.close();
            }
        });
        confirmDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        // Efek hover untuk tombol konfirmasi hapus di dalam dialog box
        confirmDeleteButton.getStyle().set("transition", "background-color 0.2s");
        confirmDeleteButton.getElement().addEventListener("mouseover", e -> confirmDeleteButton.getStyle().set("background-color", "#dc2626"));
        confirmDeleteButton.getElement().addEventListener("mouseout", e -> confirmDeleteButton.getStyle().set("background-color", ""));

        Button cancelDeleteButton = new Button("Batal", event -> confirmDialog.close());
        cancelDeleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelDeleteButton.getStyle().set("color", "#9ca3af").set("transition", "color 0.2s");
        cancelDeleteButton.getElement().addEventListener("mouseover", e -> cancelDeleteButton.getStyle().set("color", "#ffffff"));
        cancelDeleteButton.getElement().addEventListener("mouseout", e -> cancelDeleteButton.getStyle().set("color", "#9ca3af"));

        HorizontalLayout dialogFooter = new HorizontalLayout(cancelDeleteButton, confirmDeleteButton);
        dialogFooter.setJustifyContentMode(JustifyContentMode.END);
        dialogFooter.setWidthFull();
        dialogFooter.getStyle().set("margin-top", "20px");

        confirmDialog.add(dialogTitle, textInfo, warningText, dialogFooter);
        confirmDialog.open();
    }
    
}
