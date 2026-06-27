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

        // Mengaktifkan tema gelap untuk card ini beserta komponen di dalamnya
        getElement().setAttribute("theme", "dark");

        // Styling Card Container utama agar serasi dengan dashboard
        getStyle()
            .set("background-color", "#111827")
            .set("border", "1px solid rgba(0, 255, 102, 0.15)")
            .set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.05)")
            .set("border-radius", "12px")
            .set("padding", "20px")
            .set("color", "#f3f4f6");

        // Konfigurasi Input Fields agar mendukung Dark Mode & Glow Green
        fullNameField.setRequired(true);
        fullNameField.setWidthFull();
        configureDarkField(fullNameField);

        nikField.setRequired(true);
        nikField.setMaxLength(16);
        nikField.setPattern("^\\d{16}$");
        nikField.setAllowedCharPattern("[0-9]");
        nikField.setWidthFull();
        configureDarkField(nikField);

        purposeField.setRequired(true);
        purposeField.setMaxRows(3);
        purposeField.setWidthFull();
        configureDarkField(purposeField);

        // Konfigurasi Area Upload Gambar
        photoUpload.setAcceptedFileTypes("image/*");
        photoUpload.setMaxFiles(1);
        photoUpload.setWidthFull();
        photoUpload.setDropAllowed(true);

        // Kustomisasi teks dan tombol di dalam area drag-and-drop
        Span dropLabel = new Span("Drag and Drop Foto ");
        dropLabel.getStyle().set("color", "#9ca3af");

        Button selectFileButton = new Button("Pilih File");
        selectFileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        selectFileButton.getStyle()
            .set("color", "#00ff66")
            .set("font-weight", "600");

        photoUpload.setDropLabel(dropLabel);
        photoUpload.setUploadButton(selectFileButton);

        // Styling container upload
        photoUpload.getStyle()
            .set("border", "2px dashed rgba(0, 255, 102, 0.3)")
            .set("border-radius", "8px")
            .set("background-color", "rgba(255, 255, 255, 0.02)")
            .set("padding", "16px")
            .set("margin-top", "8px")
            .set("margin-bottom", "16px")
            .set("--lumo-body-text-color", "#f3f4f6")
            .set("--lumo-secondary-text-color", "#9ca3af");
        
        Div uploadLabel = new Div(new Span("Foto"));
        uploadLabel.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("margin-top", "10px")
            .set("color", "#00ff66") // Label Foto menggunakan warna Glow Green
            .set("text-shadow", "0 0 5px rgba(0, 255, 102, 0.2)");
        
        // Konfigurasi Tombol Submit
        Button submitButton = new Button("Check-in Tamu", VaadinIcon.SIGN_IN.create());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.getStyle()
            .set("background-color", "#00cc66")
            .set("color", "#090d16")
            .set("font-weight", "700")
            .set("margin-top", "16px")
            .set("box-shadow", "0 0 10px rgba(0, 255, 102, 0.2)")
            .set("transition", "all 0.2s ease-in-out");

        submitButton.getElement().addEventListener("mouseover", e -> {
            submitButton.getStyle().set("background-color", "#00ff66");
            submitButton.getStyle().set("box-shadow", "0 0 15px rgba(0, 255, 102, 0.6)");
        });
        submitButton.getElement().addEventListener("mouseout", e -> {
            submitButton.getStyle().set("background-color", "#00cc66");
            submitButton.getStyle().set("box-shadow", "0 0 10px rgba(0, 255, 102, 0.2)");
        });

        submitButton.addClickListener(event -> handleFormSubmit());

        add(fullNameField, nikField, purposeField, uploadLabel, photoUpload, submitButton);
    }

    /**
     * Helper untuk menerapkan properti style Lumo bertema gelap dan glow green pada input field.
     */
    private void configureDarkField(com.vaadin.flow.component.HasStyle field) {
        field.getStyle()
            .set("--lumo-body-text-color", "#f3f4f6")        // Warna teks input (terang)
            .set("--lumo-secondary-text-color", "#00ff66")   // Warna label (glow green)
            .set("--lumo-primary-color", "#00ff66")          // Warna outline saat fokus (glow)
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.05)") // Background input field
            .set("margin-bottom", "12px");
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
