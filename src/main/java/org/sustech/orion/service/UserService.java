package org.sustech.orion.service;

import org.sustech.orion.model.User;

import java.util.List;


public interface UserService {
    void sendVerificationCode(String email);
    void resetPassword(String email, String verificationCode, String newPassword);
    void registerNewUser(User user, String verificationCode);
    User getUserById(Long userId);
    User getUserByName(String username);
    List<User> search(String keyword);
    User updateUser(User user);

    User getReferenceById(Long userId);
}
