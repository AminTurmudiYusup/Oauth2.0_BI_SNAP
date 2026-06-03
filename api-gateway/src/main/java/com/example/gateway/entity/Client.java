package com.example.gateway.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "clientSecret")
@Table("clients")
public class Client {

    @Id
    private String clientId;

    private String clientSecret;

    private String clientName;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
