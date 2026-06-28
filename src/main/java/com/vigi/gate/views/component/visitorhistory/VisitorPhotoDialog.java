package com.vigi.gate.views.component.visitorhistory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VisitorPhotoDialog extends Dialog {

    public VisitorPhotoDialog(String photoUrl) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        // Styling dark mode pada overlay dialog
        this.getElement().executeJs(
            "this.$.overlay.$.overlay.style.backgroundColor = '#1e293b';" +
            "this.$.overlay.$.overlay.style.color = '#f1f5f9';"
        );

        H3 dialogTitle = new H3("Foto Visitor");
        dialogTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "20px")
            .set("color", "#00ff66");

        VerticalLayout photoWrapper = new VerticalLayout();
        photoWrapper.setAlignItems(Alignment.CENTER);
        photoWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        photoWrapper.setPadding(false);

        Image fullImage = new Image(photoUrl, "Foto Visitor Besar");
        fullImage.getStyle()
            .set("max-width", "100%")
            .set("max-height", "400px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.5)");
        photoWrapper.add(fullImage);

        Button closeBtn = new Button("Tutup", event -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle().set("color", "#9ca3af");

        HorizontalLayout footer = new HorizontalLayout(closeBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.END);
        footer.getStyle().set("margin-top", "16px");

        add(dialogTitle, photoWrapper, footer);
    }
    
}
