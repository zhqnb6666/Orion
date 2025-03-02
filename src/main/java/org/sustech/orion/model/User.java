package org.sustech.orion.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@Table(name = "users")
public class User implements UserDetails { // 实现 UserDetails 接口

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STUDENT;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    @Column(nullable = false)
    private String avatarUrl = "default_avatar.png";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio = "";

    // 实现 UserDetails 接口方法
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色转换为权限（例如：STUDENT -> ROLE_STUDENT）
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override
    public String getPassword() {
        return this.passwordHash; // 返回密码哈希字段
    }

    public enum Role {
        ADMIN, TEACHER, STUDENT
    }
}