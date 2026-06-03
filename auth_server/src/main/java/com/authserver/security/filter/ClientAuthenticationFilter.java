package com.authserver.security.filter;

import com.authserver.common.GeneralHelper;
import com.authserver.common.ResponseCode;
import com.authserver.common.ServiceType;
import com.authserver.exception.ApiException;
import com.authserver.exception.ErrorResponse;
import com.authserver.model.Client;
import com.authserver.security.key.KeyProvider;
import com.authserver.security.signature.SignatureValidator;
import com.authserver.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Map;

@Component
@Slf4j
public class ClientAuthenticationFilter extends OncePerRequestFilter {

    private static final Map<String, ServiceType> PATH_TO_SERVICE = Map.of(
            "/api/v1/access-token/b2b", ServiceType.ACCESS_TOKEN_B2B,
            "/api/v1/access-token/b2b2c", ServiceType.ACCESS_TOKEN_B2B2C
    );
    private final SignatureValidator signatureValidator;
    private final KeyProvider keyProvider;
    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    public ClientAuthenticationFilter(SignatureValidator signatureValidator,
                                      KeyProvider keyProvider,
                                      ClientService clientService,
                                      ObjectMapper objectMapper) {
        this.signatureValidator = signatureValidator;
        this.keyProvider = keyProvider;
        this.clientService = clientService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            log.info("Filter section log URI >>>> {}", req.getRequestURI());

            // Optional: skip non-API endpoints
            String path = req.getServletPath();

            ServiceType serviceType = PATH_TO_SERVICE.get(path);

            if (serviceType == null) {
                chain.doFilter(req, res);
                return;
            }

            String clientId = req.getHeader(GeneralHelper.VAL_X_CLIENT_KEY);
            String timestamp = req.getHeader(GeneralHelper.VAL_X_TIMESTAMP);
            String signature = req.getHeader(GeneralHelper.VAL_SIGNATURE);
            String contentType = req.getContentType();

            log.info("Headers:{}  , {}  ,{} ", clientId, timestamp, signature);

            //  Validate headers
            if (isBlank(clientId) || isBlank(timestamp) || isBlank(signature)) {
                throw new ApiException("Missing headers", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            }

            //  more flexible content-type check
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                throw new ApiException("Invalid content type", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            }

            String stringToSign = clientId + "|" + timestamp;

            Client client = clientService.getClient(clientId).orElseThrow(() -> new ApiException("Client not found", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.NOT_FOUND));

            PublicKey publicKey = keyProvider.getPublicKey(client);
            if (publicKey == null) {
                throw new ApiException("Unauthorized", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);
            }

            boolean valid = signatureValidator.validate(stringToSign, signature, publicKey);
            log.info("Signature valid ???? {}", valid);
            if (!valid) {
                throw new ApiException("Invalid signature", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);
            }

            //  Pass data forward
            req.setAttribute("clientId", clientId);

            //pas data for response
            res.setHeader(GeneralHelper.VAL_X_CLIENT_KEY, req.getHeader(GeneralHelper.VAL_X_CLIENT_KEY));
            res.setHeader(GeneralHelper.VAL_X_TIMESTAMP, req.getHeader(GeneralHelper.VAL_X_TIMESTAMP));

            chain.doFilter(req, res);

        } catch (ApiException ex) {
            handleError(req, res, ex);
        }
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, ApiException ex) throws IOException {
        String responseCode = String.format("%03d%02d%02d", ex.getHttpStatus().value(), ex.getServiceCode(), ex.getCaseCode());

        ErrorResponse error = new ErrorResponse(responseCode, ex.getMessage());

        response.setHeader(GeneralHelper.VAL_X_CLIENT_KEY, request.getHeader(GeneralHelper.VAL_X_CLIENT_KEY));
        response.setHeader(GeneralHelper.VAL_X_TIMESTAMP, request.getHeader(GeneralHelper.VAL_X_TIMESTAMP));

        response.setStatus(ex.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), error);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}