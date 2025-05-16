package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.dto.ProfileUpdateDTO;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.service.AttachmentService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final VerificationServiceImpl verificationService;
    private final EmailServiceImpl emailService;
    private final AttachmentService attachmentService;

    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, VerificationServiceImpl verificationService, EmailServiceImpl emailService, AttachmentService attachmentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.attachmentService = attachmentService;
    }

    @Override
    public User loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
        }
        //return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), new ArrayList<>());
        return user;
    }

    @Override
    public void sendVerificationCode(String email) throws ApiException {
        if (verificationService.canResendCode(email)) {
            String code = verificationService.generateVerificationCode(email);
            try {
                emailService.sendVerificationEmail(email, code);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new ApiException("Please wait before requesting a new code",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void resetPassword(String email, String verificationCode, String newPassword) {
        if (verificationService.verifyCode(email, verificationCode)) {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(Timestamp.from(Instant.now()));
            userRepository.save(user);
            verificationService.clearCode(email);
        } else {
            throw new IllegalArgumentException("Invalid verification code");
        }
    }

    @Override
    public void registerNewUser(User user, String verificationCode) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!verificationService.verifyCode(user.getEmail(), verificationCode)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        Timestamp currentTimestamp = Timestamp.from(Instant.now());
        user.setCreatedAt(currentTimestamp);
        user.setUpdatedAt(currentTimestamp);
        userRepository.save(user);
        verificationService.clearCode(user.getEmail());
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public User getUserByName(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ApiException("User NOT Found", HttpStatus.NOT_FOUND);
        }
        return user;
    }

    @Override
    public List<User> search(String keyword) {
        return userRepository.findUsersByUsernameIsContainingIgnoreCase(keyword);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getReferenceById(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, ProfileUpdateDTO profileUpdateDTO) {
        User user = getUserById(userId);

        // 更新邮箱
        if (StringUtils.hasText(profileUpdateDTO.getEmail())) {
            // 检查邮箱是否已被使用
            if (userRepository.existsByEmailAndUserIdNot(profileUpdateDTO.getEmail(), userId)) {
                throw new ApiException("Email already in use", HttpStatus.CONFLICT);
            }
            user.setEmail(profileUpdateDTO.getEmail());
        }

        // 更新个人简介
        if (profileUpdateDTO.getBio() != null) {
            user.setBio(profileUpdateDTO.getBio());
        }

        // 更新密码
        if (StringUtils.hasText(profileUpdateDTO.getNewPassword())) {
            if (!StringUtils.hasText(profileUpdateDTO.getOldPassword())) {
                throw new ApiException("Old password is required", HttpStatus.BAD_REQUEST);
            }

            // 验证旧密码
            if (!passwordEncoder.matches(profileUpdateDTO.getOldPassword(), user.getPassword())) {
                throw new ApiException("Invalid old password", HttpStatus.BAD_REQUEST);
            }

            // 设置新密码
            user.setPasswordHash(passwordEncoder.encode(profileUpdateDTO.getNewPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateAvatar(Long userId, MultipartFile file) {
        User user = getUserById(userId);

        // 上传新头像
        Attachment avatar = attachmentService.uploadAttachment(file, Attachment.AttachmentType.Resource);
        
        // 更新用户头像URL
        user.setAvatarUrl(avatar.getUrl());
        
        return userRepository.save(user);
    }
}