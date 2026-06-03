package com.authserver.security.key;

import com.authserver.model.Client;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyProvider {

    public PublicKey getPublicKey(Client client) {

        try {
            byte[] keyBytes = Base64.getDecoder().decode(client.getPublicKey());

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(spec);

        } catch (Exception e) {
            throw new RuntimeException("Invalid public key", e);
        }
    }
}
