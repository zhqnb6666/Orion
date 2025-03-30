package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = {"assignment"})
@Table(name = "code_assignment_configs")
public class CodeAssignmentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 允许的编程语言列表，格式为以逗号分隔的字符串，如："java,python,cpp"
    @Column(nullable = false)
    private String allowedLanguages;

    // 是否启用内存限制
    @Column(nullable = false)
    private Boolean memoryLimitEnabled = false;

    // 内存限制（单位：MB）
    @Column
    private Integer memoryLimitMB;

    // 是否启用执行时间限制
    @Column(nullable = false)
    private Boolean timeLimitEnabled = false;

    // 执行时间限制（单位：秒）
    @Column
    private Integer timeLimitSeconds;

    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
} 