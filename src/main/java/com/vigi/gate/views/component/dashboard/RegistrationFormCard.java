package com.vigi.gate.views.component.dashboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vigi.gate.dto.VisitorRegistrationRequest;
import com.vigi.gate.service.VisitorManagementService;
import com.vigi.gate.views.MainView;
import com.vigi.gate.views.component.BaseCard;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrationFormCard extends BaseCard {

    private final VisitorManagementService visitorManagementService;
    private final MainView mainView;

    private final TextField fullNameField = new TextField("Nama");
    private final TextField nikField = new TextField("NIK (16 digit)");
    private final TextArea purposeField = new TextArea("Tujuan");
    private MemoryBuffer buffer = new MemoryBuffer();
    private final Upload photoUpload = new Upload(buffer);

    public RegistrationFormCard(VisitorManagementService visitorManagementService, MainView mainView) {
        super("Smart Registration");
        this.visitorManagementService = visitorManagementService;
        this.mainView = mainView;

        fullNameField.setRequired(true);
        fullNameField.setWidthFull();

        nikField.setRequired(true);
        nikField.setMaxLength(16);
        nikField.setPattern("^\\d{16}$");
        nikField.setAllowedCharPattern("[0-9]");
        nikField.setWidthFull();

        purposeField.setRequired(true);
        purposeField.setMaxRows(3);
        purposeField.setWidthFull();

        photoUpload.setAcceptedFileTypes("image/*");
        photoUpload.setMaxFiles(1);
        photoUpload.setWidthFull();
        photoUpload.setDropAllowed(true);
        
        Div uploadLabel = new Div(new Span("Foto"));
        uploadLabel.getStyle().set("font-size", "14px").set("font-weight", "600").set("margin-top", "10px");
        
        Button submitButton = new Button("Check-in Tamu", VaadinIcon.SIGN_IN.create());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.getStyle().set("background-color", "#2563eb").set("margin-top", "12px");

        submitButton.addClickListener(event -> handleFormSubmit());

        add(fullNameField, nikField, purposeField, uploadLabel, photoUpload, submitButton);
    }

    private void handleFormSubmit() {
        String nik = nikField.getValue().trim();
        if (fullNameField.isEmpty() || nik.length() != 16 || purposeField.isEmpty()) {
            Notification.show("Pastikan semua form diisi dengan benar dan NIK 16 digit.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        VisitorRegistrationRequest request = new VisitorRegistrationRequest();
        request.setFullName(fullNameField.getValue());
        request.setNik(nik);
        request.setPurpose(purposeField.getValue());

        MultipartFile multipartFile = null;
        if (!buffer.getFileName().isEmpty()) {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                final String fileName = buffer.getFileName();
                final String contentType = buffer.getFileData().getMimeType();
                
                multipartFile = new MultipartFile() {
                    @Override public String getName() { return "photo"; }
                    @Override public String getOriginalFilename() { return fileName; }
                    @Override public String getContentType() { return contentType; }
                    @Override public boolean isEmpty() { return bytes.length == 0; }
                    @Override public long getSize() { return bytes.length; }
                    @Override public byte[] getBytes() throws IOException { return bytes; }
                    @Override public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(bytes); }
                    @Override public void transferTo(java.io.File dest) throws IOException, IllegalStateException {}
                };
            } catch (IOException e) {
                log.error("Gagal membaca file gambar", e);
            }
        }

        try {
            var response = visitorManagementService.registerVisitor(request, multipartFile);
            Notification.show("Check-in berhasil. Risk " + response.getRiskLevel() + " (score " + response.getRiskScore() + ").", 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Reset Form
            fullNameField.clear();
            nikField.clear();
            purposeField.clear();
            buffer = new MemoryBuffer();

            photoUpload.setReceiver(buffer);
            photoUpload.clearFileList();

            // Refresh seluruh data di view induk
            mainView.refreshAllData();
        } catch (Exception e) {
            Notification.show("Gagal check-in: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
}
