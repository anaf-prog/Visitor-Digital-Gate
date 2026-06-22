package com.vigi.gate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.vigi.gate.views.LoginView;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Mengizinkan akses publik ke folder resource statis seperti gambar bawaan jika ada
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/images/**").permitAll()
        );

        // Menyerahkan pengaturan keamanan internal routing dan websocket ke VaadinWebSecurity
        super.configure(http);

        // Menentukan bahwa LoginView.class milik Vaadin adalah halaman login resmi aplikasi ini
        setLoginView(http, LoginView.class, "/");
    }

    // Bean ini digunakan untuk melakukan hashing password di database
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
