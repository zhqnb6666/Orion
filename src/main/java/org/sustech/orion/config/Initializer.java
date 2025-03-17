package org.sustech.orion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.service.impl.UserServiceImpl;
import org.sustech.orion.util.JwtUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class Initializer {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    private final UserRepository userRepository;

    private final CourseRepository courseRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserServiceImpl userService;

    private final JwtUtil jwtUtil;

    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            // 只在 ddl-auto = "create" 时执行初始化
            if ("create".equalsIgnoreCase(ddlAuto)) {
                initDatabase();
                generateAndPrintJwt();
            } else {
                generateAndPrintJwt();
                System.out.println("DDL-Auto is not set to 'create', skipping database initialization.");
            }
        };
    }


    public void initDatabase() {
        Map<String, User> users = createUsers();
        Course course = createCourse(users.get("teacher"), users.get("student"));
    }

    public Map<String, User> createUsers() {
        User admin = createUser("admin", "admin@example.com", User.Role.ADMIN);
        User teacher = createUser("teacher", "teacher@example.com", User.Role.TEACHER);
        User student = createUser("student", "student@example.com", User.Role.STUDENT);
        // 系统用户，用于发送系统通知
        User system = createUser("SYSTEM", "system@example.com", User.Role.ADMIN);
        Map<String, User> userMap = new HashMap<>();
        userMap.put("admin", admin);
        userMap.put("teacher", teacher);
        userMap.put("student", student);
        userMap.put("system", system);
        System.out.println("Created users: admin, teacher, student");
        return userMap;
    }

    private User createUser(String username, String mail, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(mail);
        user.setPasswordHash(passwordEncoder.encode(username));
        user.setRole(role);
        user.setCreatedAt(Timestamp.from(Instant.now()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        return userRepository.save(user);
    }

    private Course createCourse(User instructor, User student) {
        Course course = new Course();
        course.setCourseName("test course");
        course.setCourseCode("TEST-000");
        course.setDescription("test description");
        course.setInstructor(instructor);
        course.setStudents(List.of(student));
        course.setSemester("2025-Spring");
        course.setIsActive(true);
        course.setCreatedTime(Timestamp.from(Instant.now()));
        return courseRepository.save(course);
    }

    private void generateAndPrintJwt() {
        String[] usernames = {"admin", "teacher", "student"};
        Map<String, String> jwtMap = new HashMap<>();
        for (String username : usernames) {
            UserDetails userDetails = userService.loadUserByUsername(username);
            String jwt = jwtUtil.generateToken(userDetails);
            jwtMap.put(username, jwt);
        }
        for (Map.Entry<String, String> entry : jwtMap.entrySet()) {
            System.out.println("Generated JWT for " + entry.getKey() + ": " + entry.getValue());
        }
    }

}
