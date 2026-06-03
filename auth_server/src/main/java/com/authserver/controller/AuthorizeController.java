package com.authserver.controller;

import com.authserver.common.ResponseCode;
import com.authserver.common.ServiceType;
import com.authserver.exception.ApiException;
import com.authserver.model.Client;
import com.authserver.repository.ClientRepository;
import com.authserver.repository.ScopeRepository;
import com.authserver.service.AuthorizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Tag(
        name = "Authorization API",
        description = "Handles OAuth2 authorization flow including user consent and authorization code generation"
)
@Controller
public class AuthorizeController {
    @Autowired
    private ClientRepository clientRepository;


    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private ScopeRepository scopeRepository;

    @Operation(
            summary = "Authorize Client Application",
            description = "Initiates the OAuth2 authorization process. " +
                    "Validates the client_id and requested scopes. " +
                    "If the user is authenticated, a consent page is displayed. " +
                    "Otherwise, the user is redirected to login."
    )
    @GetMapping("${path.root.oauth2}" + "authorize")
    public String authorize(
            @RequestParam String client_id,
            @RequestParam String scope,
            @RequestParam String redirect_uri,
            Model model,
            Principal principal) {

        // 1. Validate client
        Optional<Client> client = clientRepository.findByClientId(client_id);

        if (client.isEmpty()) {
            throw new ApiException("Invalid client id", ServiceType.ACCESS_TOKEN_B2B.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
        }

        List<String> scopes = Arrays.stream(scope.split(" "))
                .map(String::trim)
                .toList();
        for (String s : scopes) {
            if (scopeRepository.findByName(s).isEmpty()) {
                throw new ApiException("scope name not found!", ServiceType.ACCESS_TOKEN_B2B.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            }
        }
        // 2. If not logged in → Spring Security will handle redirect


        // 3. Show consent page
        model.addAttribute("client", client.get());
        model.addAttribute("redirectUri", redirect_uri);
        model.addAttribute("scopes", scopes);
        model.addAttribute("scope", scope);

        return "consent";
    }

    @Operation(
            summary = "Approve or Deny Authorization Request",
            description = "Handles user decision from consent screen. " +
                    "If approved, generates an authorization code and redirects to client. " +
                    "If denied, redirects with an access_denied error."
    )
    @PostMapping("${path.root.oauth2}" + "authorize")
    @PreAuthorize("isAuthenticated()")
    public void approve(
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam String decision,
            @RequestParam String scope,
            HttpServletResponse response,
            Principal principal) throws IOException {

        if ("yes".equals(decision)) {

            String code = authorizeService.generateAuthorizationCode(client_id, principal.getName(), redirect_uri, scope);
            authorizeService.savePermission(client_id, scope);
            response.sendRedirect(redirect_uri + "?code=" + code);

        } else {
            response.sendRedirect(redirect_uri + "?error=access_denied");
        }
    }


}
