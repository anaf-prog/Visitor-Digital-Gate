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

    public RiskRuleTableCard(RiskRuleService riskRuleService, RiskruleView riskruleView) {
        super("Daftar Risk Rule"); // Set judul card induk
        this.riskRuleService = riskRuleService;
        this.riskruleView = riskruleView;

        grid.addColumn(RiskRule::getRuleName).setHeader("Rule").setSortable(true);
        grid.addColumn(rule -> rule.getConditionType() + ": " + rule.getConditionValue()).setHeader("Kondisi");
        grid.addColumn(RiskRule::getRiskLevel).setHeader("Risk");
        grid.addColumn(RiskRule::getScoreContribution).setHeader("Score");
        
        grid.addColumn(new ComponentRenderer<Span, RiskRule>(rule -> {
            Span statusBadge = new Span(rule.isActive() ? "ACTIVE" : "INACTIVE");
            if (rule.isActive()) {
                statusBadge.getStyle().set("color", "#166534").set("font-weight", "700");
            } else {
                statusBadge.getStyle().set("color", "#6b7280").set("font-weight", "700");
            }
            return statusBadge;
        })).setHeader("Status").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(rule -> {
            Button editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.getStyle().set("background-color", "#d97706").set("color", "#fff");
            editBtn.addClickListener(e -> riskruleView.triggerEditMode(rule));

            Button deleteBtn = new Button("Hapus");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> showDeleteConfirmation(rule));

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

        H3 dialogTitle = new H3("Konfirmasi Hapus");
        dialogTitle.getStyle().set("margin-top", "0");
        
        Paragraph textInfo = new Paragraph();
        textInfo.add("Apakah Anda yakin ingin menghapus risk rule ");
        Span ruleNameHighlight = new Span(rule.getRuleName());
        ruleNameHighlight.getStyle().set("font-weight", "700");
        textInfo.add(ruleNameHighlight);
        textInfo.add("?");

        Paragraph warningText = new Paragraph("Tindakan ini tidak dapat dibatalkan.");
        warningText.getStyle().set("color", "#dc2626").set("font-weight", "700").set("font-size", "13px");

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

        Button cancelDeleteButton = new Button("Batal", event -> confirmDialog.close());
        cancelDeleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout dialogFooter = new HorizontalLayout(cancelDeleteButton, confirmDeleteButton);
        dialogFooter.setJustifyContentMode(JustifyContentMode.END);
        dialogFooter.setWidthFull();
        dialogFooter.getStyle().set("margin-top", "20px");

        confirmDialog.add(dialogTitle, textInfo, warningText, dialogFooter);
        confirmDialog.open();
    }
    
}
