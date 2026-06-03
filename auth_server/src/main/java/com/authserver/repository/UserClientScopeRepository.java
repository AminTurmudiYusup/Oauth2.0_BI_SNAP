package com.authserver.repository;

import com.authserver.model.Client;
import com.authserver.model.Scope;
import com.authserver.model.User;
import com.authserver.model.UserClientScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserClientScopeRepository extends JpaRepository<UserClientScope, String> {
    boolean existsByUserAndClientAndScope(User user, Client client, Scope scope);

    List<UserClientScope> findByUserAndClient(User user, Client client);
}
