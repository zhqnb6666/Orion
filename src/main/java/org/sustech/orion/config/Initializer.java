package org.sustech.orion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.sustech.orion.model.*;
import org.sustech.orion.repository.*;
import org.sustech.orion.service.impl.UserServiceImpl;
import org.sustech.orion.util.FileSizeUtil;
import org.sustech.orion.util.JwtUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final NotificationRepository notificationRepository;
    private final AssignmentRepository assignmentRepository;
    private final ResourceRepository resourceRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final AttachmentRepository attachmentRepository;

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

        createTestNotification(users.get("system"), users.get("student"),
                "Test system Notification", "This is a test notification with high priority",
                Notification.Priority.HIGH);
        createTestNotification(users.get("teacher"), users.get("student"),
                "Test teacher Notification", "This is a test notification with medium priority",
                Notification.Priority.MEDIUM);

        Attachment pic = createAttachment("test picture",
                "https://bkimg.cdn.bcebos.com/pic/0b46f21fbe096b63f624d0f97e6a9044ebf81a4c065a?x-bce-process=image/format,f_auto/watermark,image_d2F0ZXIvYmFpa2UyNzI,g_7,xp_5,yp_5,P_20/resize,m_lfit,limit_1,h_1080");
        Attachment pdf = createAttachment("test pdf",
                "https://www.sustech.edu.cn/uploads/files/2024/12/24093901_29176.pdf");
        Attachment codeFile = createAttachment("test code file",
                "https://docs.oracle.com/javase/tutorial/essential/concurrency/examples/SimpleThreads.java");

        createResource(course, List.of(pic, pdf, codeFile));

        Assignment closedAssignment = createAssignment("test closed assignment", course, List.of(pic),
                Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)),
                Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        Assignment openAssignment = createAssignment("test open assignment", course, List.of(pdf),
                Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)),
                Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)));
        Assignment upcomingAssignment = createAssignment("test upcoming assignment", course, List.of(codeFile),
                Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)),
                Timestamp.from(Instant.now().plus(60, ChronoUnit.DAYS)));

        SubmissionContent pdfContent = createSubmissionContent(SubmissionContent.ContentType.FILE, null,
                "https://www.sustech.edu.cn/uploads/files/2024/12/24093901_29176.pdf", "application/pdf");
        SubmissionContent codeContent = createSubmissionContent(SubmissionContent.ContentType.FILE, null,
                "https://docs.oracle.com/javase/tutorial/essential/concurrency/examples/SimpleThreads.java", "text/x-java-source");
        SubmissionContent textContent = createSubmissionContent(SubmissionContent.ContentType.TEXT,
                "text submission content", null, null);

        Submission submission1 = createSubmission(closedAssignment, users.get("student"),
                Timestamp.from(Instant.now().minus(5, ChronoUnit.DAYS)),
                List.of(pdfContent), 1);
        Submission submission2 = createSubmission(openAssignment, users.get("student"),
                Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS)),
                List.of(textContent), 1);
        Submission submission3 = createSubmission(upcomingAssignment, users.get("student"),
                Timestamp.from(Instant.now()),
                List.of(codeContent), 2);

        createGrade(submission1, users.get("teacher"), 90.0, "good job",
                Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
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

    private void createTestNotification(User sender, User recipient, String title, String content, Notification.Priority priority) {
        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setPriority(priority);
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notificationRepository.save(notification);
    }

    private Attachment createAttachment(String name, String url) {
        Attachment attachment = new Attachment();
        attachment.setName(name);
        attachment.setUrl(url);
        attachment.setSize(FileSizeUtil.getFileSize(url));
        return attachmentRepository.save(attachment);
    }

    private void createResource(Course course, List<Attachment> attachments) {
        Resource resource = new Resource();
        resource.setName("test resource");
        resource.setDescription("test description");
        resource.setType("test");
        resource.setCourse(course);
        resource.setUploadedBy(course.getInstructor());
        resource.setUploadTime(Timestamp.from(Instant.now()));
        resource.setAttachments(attachments);
        resourceRepository.save(resource);
    }

    private Assignment createAssignment(String title, Course course, List<Attachment> attachments, Timestamp openDate, Timestamp dueDate) {
        Assignment assignment = new Assignment();
        assignment.setTitle("test assignment");
        assignment.setDescription("test description");
        assignment.setType("test");
        assignment.setCourse(course);
        assignment.setAttachments(attachments);
        assignment.setInstructions("test instructions");
        assignment.setOpenDate(openDate);
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(100);
        return assignmentRepository.save(assignment);
    }

    private SubmissionContent createSubmissionContent(SubmissionContent.ContentType type, String content, String fileUrl, String mimeType) {
        SubmissionContent submissionContent = new SubmissionContent();
        submissionContent.setType(type);
        if (type == SubmissionContent.ContentType.FILE) {
            submissionContent.setFileUrl(fileUrl);
            submissionContent.setMimeType(mimeType);
            submissionContent.setFileSize(FileSizeUtil.getFileSize(fileUrl));
        } else {
            submissionContent.setContent(content);
        }
        return submissionContent;
    }

    private Submission createSubmission(Assignment assignment, User student, Timestamp submissionTime,
                                        List<SubmissionContent> contents, Integer attempts) {
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSubmitTime(submissionTime);
        submission.setStatus(Submission.SubmissionStatus.PENDING);
        submission.setContents(contents);
        submission.setAttempts(attempts);
        for (SubmissionContent content : contents) {
            content.setSubmission(submission);
        }
        return submissionRepository.save(submission);
    }

    private void createGrade(Submission submission, User grader, Double score, String feedback, Timestamp gradedTime) {
        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setGrader(grader);
        grade.setScore(score);
        grade.setFeedback(feedback);
        grade.setGradedTime(gradedTime);
        grade.setIsFinalized(true);
        grade.setStatus(Grade.Status.GRADED);
        gradeRepository.save(grade);
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
