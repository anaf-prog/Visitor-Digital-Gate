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
        getStyle().set("background-color", "#f8fafc");

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

        // Menghubungkan Aksi Submit Form langsung ke Endpoint Login standar Spring Security
        loginForm.setAction("login");

        VerticalLayout cardWrapper = new VerticalLayout();
        cardWrapper.setAlignItems(Alignment.CENTER);
        cardWrapper.setPadding(true);
        cardWrapper.setSpacing(true);
        cardWrapper.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)")
            .set("max-width", "420px")
            .set("padding", "24px");

        H1 title = new H1("Vigi Gate");
        title.getStyle()
            .set("color", "#2563eb")
            .set("margin", "0")
            .set("font-size", "28px")
            .set("font-weight", "800");

        Paragraph subtitle = new Paragraph("Digital Visitor Management System");
        subtitle.getStyle()
            .set("color", "#64748b")
            .set("margin-top", "0")
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
