package com.ocs.auth.config;

import com.ocs.auth.model.Role;
import com.ocs.auth.model.User;
import com.ocs.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DefaultAdminConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.default-admin.username}")
    private String defaultAdminUsername;

    @Value("${application.default-admin.password}")
    private String defaultAdminPassword;

    @Value("${application.default-admin.email}")
    private String defaultAdminEmail;

    @Value("${application.default-admin.firstname}")
    private String defaultAdminFirstName;

    @Value("${application.default-admin.lastname}")
    private String defaultAdminlastName;

    @Bean
    public CommandLineRunner createDefaultAdmin() {
        return args -> {
            // Check if superadmin already exists
            if (userRepository.findByUsername(defaultAdminUsername).isEmpty()) {
                User superAdmin = User.builder()
                        .firstName("Super")
                        .lastName("Admin")
                        .username(defaultAdminUsername)
                        .password(passwordEncoder.encode(defaultAdminPassword))
                        .email(defaultAdminEmail)
                        .role(Role.SUPER_ADMIN)
                        .build();
                
                userRepository.save(superAdmin);
                System.out.println("Default superadmin user created successfully!");
            }
        };
    }
} 