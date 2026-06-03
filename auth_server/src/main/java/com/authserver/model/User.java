package com.authserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    private String passwordHash;
    private String email;

    private String status;
    private Integer failedAttempts;

    private LocalDateTime lockedUntil;
    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🕒 Auto timestamps
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
