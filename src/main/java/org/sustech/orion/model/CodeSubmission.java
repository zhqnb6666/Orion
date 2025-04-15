package org.sustech.orion.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "code")
@Entity
public class CodeSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String script;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private Integer VersionIndex;
}
