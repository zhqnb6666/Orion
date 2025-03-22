package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = true)
    private boolean isRead;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "sender_id")
    private User sender; // new

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Getter
    public enum Priority {
        HIGH("High Priority"),
        MEDIUM("Medium Priority"),
        LOW("Low Priority");
        private final String value;

        Priority(String value) {
            this.value = value;
        }
    }

}
