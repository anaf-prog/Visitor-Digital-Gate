package com.vigi.gate.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Vigi Gate")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

     private final LoginForm loginForm = new LoginForm();

    public LoginView() {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        // Menggunakan warna background gelap ruang angkasa (Deep Space Dark)
        getStyle().set("background-color", "#090d16");

        // --- Injeksi CSS untuk warna Hijau Glow, Teks Gelap, dan Label Aktif ---
        com.vaadin.flow.dom.Element styleElement = new com.vaadin.flow.dom.Element("style");
        styleElement.setText(
            "vaadin-login-form, vaadin-login-form-wrapper {\n" +
            "  --lumo-primary-color: #00ff66 !important;\n" + // Background tombol masuk (hijau neon)
            "  --lumo-primary-contrast-color: #090d16 !important;\n" + // Warna teks tombol masuk (gelap agar kontras)
            "  --lumo-primary-text-color: #00ff66 !important;\n" + // Mengubah teks/label username & password menjadi hijau saat fokus
            "  --lumo-primary-color-50pct: rgba(0, 255, 102, 0.5) !important;\n" + // Hover state & focus border
            "  --lumo-primary-color-10pct: rgba(0, 255, 102, 0.1) !important;\n" + // Focus ring
            "  --lumo-button-shadow: 0 0 15px rgba(0, 255, 102, 0.5) !important;\n" + // Efek glow tombol
            "}\n" +
            "vaadin-button[theme~='primary'] {\n" +
            "  box-shadow: var(--lumo-button-shadow) !important;\n" +
            "}"
        );
        getElement().appendChild(styleElement);
        // ------------------------------------------------------------------------

        // Mengatur Teks dan Label Komponen Login ke Bahasa Indonesia (I18n)
        LoginI18n i18n = LoginI18n.createDefault();
        
        LoginI18n.Form formLabels = i18n.getForm();
        formLabels.setTitle("Log In Admin");
        formLabels.setUsername("Username");
        formLabels.setPassword("Password");
        formLabels.setSubmit("Masuk");
        i18n.setForm(formLabels);

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Gagal Masuk");
        errorMessage.setMessage("Username atau password salah. Silakan periksa kembali kredensial Anda.");
        i18n.setErrorMessage(errorMessage);

        loginForm.setI18n(i18n);
        
        loginForm.setForgotPasswordButtonVisible(false);

        // Menghubungkan Aksi Submit Form langsung ke Endpoint Login
        loginForm.setAction("login");

        // Memaksa elemen internal LoginForm untuk menggunakan tema gelap Lumo
        loginForm.getElement().setAttribute("theme", "dark");
        
        // Tambahan style untuk memastikan wrapper internal form tidak memaksakan background putih
        loginForm.getStyle().set("background-color", "transparent");
        loginForm.getStyle().set("box-shadow", "none");

        VerticalLayout cardWrapper = new VerticalLayout();
        cardWrapper.setAlignItems(Alignment.CENTER);
        cardWrapper.setPadding(true);
        cardWrapper.setSpacing(true);
        
        // Mengubah warna panel login ke dark card dengan aksen neon border & glow
        cardWrapper.getStyle()
            .set("background", "#111827")
            .set("border", "1px solid rgba(0, 255, 102, 0.2)")
            .set("border-radius", "16px")
            .set("box-shadow", "0 0 25px rgba(0, 255, 102, 0.1)")
            .set("max-width", "420px")
            .set("padding", "32px");

        H1 title = new H1("Vigi Gate");
        // Memberikan efek glow in the dark pada title utama
        title.getStyle()
            .set("color", "#00ff66")
            .set("text-shadow", "0 0 12px rgba(0, 255, 102, 0.6)")
            .set("margin", "0")
            .set("font-size", "32px")
            .set("font-weight", "900")
            .set("letter-spacing", "1px");

        Paragraph subtitle = new Paragraph("Digital Visitor Management System");
        // Menyesuaikan warna teks subtitle dengan tema dark mode
        subtitle.getStyle()
            .set("color", "#9ca3af")
            .set("margin-top", "4px")
            .set("margin-bottom", "16px")
            .set("font-size", "14px");

        cardWrapper.add(title, subtitle, loginForm);

        // Tambahkan pembungkus ke halaman utama LoginView
        add(cardWrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
    
}
