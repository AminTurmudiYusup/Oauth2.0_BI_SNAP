package com.authserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_client_scopes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "client_id", "scope_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserClientScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔗 Client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // 🔗 Scope
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id", nullable = false)
    private Scope scope;

    // 🕒 Granted time
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @PrePersist
    public void prePersist() {
        this.grantedAt = LocalDateTime.now();
    }
}
