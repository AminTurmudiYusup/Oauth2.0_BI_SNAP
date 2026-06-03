package com.authserver.repository;

import com.authserver.model.AuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, String> {
    Optional<AuthorizationCode> findByCodeAndUsedFalse(String code);
}
