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

    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, VerificationServiceImpl verificationService, EmailServiceImpl emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
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
                .orElseThrow(() -> new ApiException("User NOT Found", HttpStatus.NOT_FOUND));
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


}