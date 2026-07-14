package com.paw.users.seeder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.model.User;
import com.paw.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${pawplus.admin.email:admin@pawplus.cl}")
    private String adminEmail;

    @Value("${pawplus.admin.password:Admin12345!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        boolean existeAdmin = userRepository.existsByEmailIgnoreCase(adminEmail);

        if (!existeAdmin) {
            User admin = User.builder()
                    .email(adminEmail.toLowerCase())
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("Sistema")
                    .lastName("Administrador")
                    .role(UserRole.ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
        }
    }
}