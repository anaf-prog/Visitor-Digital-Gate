package com.vigi.gate.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vigi.gate.entity.Users;
import com.vigi.gate.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSeedeer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Cek jika sudah ada user di database, jangan jalankan seeder lagi
        if (userRepository.count() > 0) {
            return;
        }

        /*
         * Seeder default hanya dijalankan saat tabel users masih kosong.
         */
        userRepository.saveAll(List.of(
            createUser("admin", "admin@vigi.com", "Admin123@", "ADMIN"),
            createUser("petugas", "petugas@vigi.com", "Petugas123@", "USER")
        ));
    }

    /**
     * Helper method untuk membuat objek Users
     */
    private Users createUser(String username, String email, String plainPassword, String role) {
        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);

        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(hashedPassword);
        
        user.setRole(role);
        return user;
    }
    
}
