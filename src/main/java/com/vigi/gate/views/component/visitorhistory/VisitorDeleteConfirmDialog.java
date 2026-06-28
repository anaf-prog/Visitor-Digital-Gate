package com.vigi.gate.views.component.visitorhistory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.VistorHistoryView;

public class VisitorDeleteConfirmDialog extends Dialog {

    public VisitorDeleteConfirmDialog(VisitorLogResponse res, 
                                      VisitorManagementService visitorManagementService, 
                                      VistorHistoryView vistorHistoryView) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        this.getElement().executeJs(
            "this.$.overlay.$.overlay.style.backgroundColor = '#1e293b';" +
            "this.$.overlay.$.overlay.style.color = '#f1f5f9';"
        );

        H3 dialogTitle = new H3("Konfirmasi Hapus Data");
        dialogTitle.getStyle().set("margin-top", "0").set("color", "#f87171");

        Paragraph bodyText = new Paragraph("Apakah Anda yakin ingin menghapus data visitor " + res.getFullName() + "?");
        bodyText.getStyle().set("color", "#9ca3af"); 

        Paragraph warningText = new Paragraph("Tindakan ini tidak dapat dibatalkan.");
        warningText.getStyle().set("color", "#f87171").set("font-weight", "700").set("font-size", "13px");

        Button confirmBtn = new Button("Hapus", event -> {
            try {
                visitorManagementService.deleteVisitor(res.getLogId());
                Notification.show("Data visitor berhasil dihapus.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
                
                vistorHistoryView.refreshHistoryData();
            } catch (Exception ex) {
                Notification.show("Gagal menghapus data visitor.", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                close();
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        confirmBtn.getStyle().set("transition", "background-color 0.2s");
        confirmBtn.getElement().addEventListener("mouseover", e -> confirmBtn.getStyle().set("background-color", "#dc2626"));
        confirmBtn.getElement().addEventListener("mouseout", e -> confirmBtn.getStyle().set("background-color", ""));

        Button cancelBtn = new Button("Batal", event -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.getStyle().set("color", "#9ca3af").set("transition", "color 0.2s");
        cancelBtn.getElement().addEventListener("mouseover", e -> cancelBtn.getStyle().set("color", "#ffffff"));
        cancelBtn.getElement().addEventListener("mouseout", e -> cancelBtn.getStyle().set("color", "#9ca3af"));

        HorizontalLayout footerLayout = new HorizontalLayout(cancelBtn, confirmBtn);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(JustifyContentMode.END);
        footerLayout.getStyle().set("margin-top", "20px");

        add(dialogTitle, bodyText, warningText, footerLayout);
    }
    
}
