package org.sustech.orion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.service.impl.UserServiceImpl;
import org.sustech.orion.util.JwtUtil;

import java.sql.Timestamp;
import java.time.Instant;

@Configuration
public class Initializer {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserServiceImpl userService;

    private final JwtUtil jwtUtil;

    public Initializer(UserRepository userRepository, PasswordEncoder passwordEncoder, UserServiceImpl userService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            // 只在 ddl-auto = "create" 时执行初始化
            if ("create".equalsIgnoreCase(ddlAuto)) {
                initDatabase();
                generateAndPrintAdminJwt();
            } else {
                generateAndPrintAdminJwt();
                System.out.println("DDL-Auto is not set to 'create', skipping database initialization.");
            }
        };
    }


    public void initDatabase() {
        createAdmin();
    }

    public void createAdmin() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash(passwordEncoder.encode("admin"));
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setCreatedAt(Timestamp.from(Instant.now()));
        adminUser.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(adminUser);
        System.out.println("Admin user created.");
    }

    private void generateAndPrintAdminJwt() {
        UserDetails adminDetails = userService.loadUserByUsername("admin");
        String jwt = jwtUtil.generateToken(adminDetails);
        System.out.println("Generated JWT for admin: " + jwt);
    }

}
