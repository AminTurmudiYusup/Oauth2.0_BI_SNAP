package com.authserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    private String clientId;
    @Column(columnDefinition = "TEXT")
    private String publicKey;//for  asymetric
    @Column(columnDefinition = "TEXT")
    private String clientSecret;//symetric
    private String clientName;
    private String status;
    private String scope;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
