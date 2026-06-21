package com.vigi.gate.views.component.riskrule;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vigi.gate.dto.RiskRuleForm;
import com.vigi.gate.entity.RiskRule;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.service.RiskRuleService;
import com.vigi.gate.views.RiskruleView;
import com.vigi.gate.views.component.BaseCard;

public class RiskRuleFormCard extends BaseCard {

    private final RiskRuleService riskRuleService;
    private final RiskruleView riskruleView;

    // Komponen Input Form
    private final TextField ruleName = new TextField("Nama Rule");
    private final ComboBox<String> conditionType = new ComboBox<>("Tipe Kondisi");
    private final TextField conditionValue = new TextField("Nilai Kondisi");
    private final ComboBox<RiskLevel> riskLevel = new ComboBox<>("Risk Level");
    private final IntegerField scoreContribution = new IntegerField("Score Contribution");
    private final Checkbox active = new Checkbox("Active");
    
    // Komponen Tombol Form Kontrol
    private final Button saveButton = new Button("Simpan Rule");
    private final Button cancelButton = new Button("Batal Edit");
    private final H3 formTitle = new H3("Tambah Rule Baru");

    // Binder Data untuk Sinkronisasi Objek Form
    private final Binder<RiskRuleForm> binder = new BeanValidationBinder<>(RiskRuleForm.class);
    
    // State Tracking untuk Mode Edit
    private RiskRuleForm currentFormObject = new RiskRuleForm();
    private Long editingId = null;

    public RiskRuleFormCard(RiskRuleService riskRuleService, RiskruleView riskruleView) {
        super(""); // Kosongkan karena memakai title dinamis `formTitle` di bawah
        this.riskRuleService = riskRuleService;
        this.riskruleView = riskruleView;

        formTitle.getStyle().set("margin-top", "0");
        add(formTitle);

        ruleName.setPlaceholder("Contoh: Late Night Visit");
        ruleName.setWidthFull();

        conditionType.setItems("TIME", "FREQUENCY");
        conditionType.setPlaceholder("-- Pilih --");
        conditionType.setWidthFull();

        conditionValue.setPlaceholder("Contoh: 22-05 atau 10");
        conditionValue.setWidthFull();

        riskLevel.setItems(RiskLevel.values());
        riskLevel.setPlaceholder("-- Pilih --");
        riskLevel.setWidthFull();

        scoreContribution.setMin(0);
        scoreContribution.setMax(100);
        scoreContribution.setWidthFull();

        HorizontalLayout checkboxRow = new HorizontalLayout(active);
        checkboxRow.setPadding(false);
        checkboxRow.getStyle().set("margin-top", "10px");

        // Pemetaan properti form DTO
        binder.forField(ruleName).bind("ruleName");
        binder.forField(conditionType).bind("conditionType");
        binder.forField(conditionValue).bind("conditionValue");
        binder.forField(riskLevel).bind("riskLevel");
        binder.forField(scoreContribution).bind("scoreContribution");
        binder.forField(active).bind("active");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("background-color", "#2563eb");
        saveButton.addClickListener(event -> handleSave());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setVisible(false);
        cancelButton.addClickListener(event -> resetForm());

        HorizontalLayout buttonRow = new HorizontalLayout(saveButton, cancelButton);
        buttonRow.setSpacing(true);
        buttonRow.getStyle().set("margin-top", "14px");

        FormLayout formFieldsLayout = new FormLayout(ruleName, conditionType, conditionValue, riskLevel, scoreContribution);
        formFieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        add(formFieldsLayout, checkboxRow, buttonRow);

        // Load awal data bean kosong ke binder
        binder.readBean(currentFormObject);
    }

    private void handleSave() {
        try {
            binder.writeBean(currentFormObject);
            
            if (editingId != null) {
                riskRuleService.updateRule(editingId, currentFormObject);
                Notification.show("Risk rule berhasil diubah.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                riskRuleService.createRule(currentFormObject);
                Notification.show("Risk rule berhasil ditambahkan.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            
            resetForm();
            riskruleView.refreshAllData();
            
        } catch (ValidationException e) {
            Notification.show("Mohon periksa kembali isian form Anda.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void enterEditMode(RiskRule rule) {
        editingId = rule.getId();
        formTitle.setText("Edit Rule");
        saveButton.setText("Update Rule");
        cancelButton.setVisible(true);

        currentFormObject = riskRuleService.toForm(rule);
        binder.readBean(currentFormObject);
    }

    public void resetForm() {
        editingId = null;
        formTitle.setText("Tambah Rule Baru");
        saveButton.setText("Simpan Rule");
        cancelButton.setVisible(false);
        
        currentFormObject = new RiskRuleForm();
        binder.readBean(currentFormObject);
    }

    public void handleExternalDelete(Long deletedId) {
        if (editingId != null && editingId.equals(deletedId)) {
            resetForm();
        }
    }
    
}
