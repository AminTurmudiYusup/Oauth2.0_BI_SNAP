package com.authserver.security.signature;

import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

@Component
public class SignatureValidator {
    public boolean validate(String data, String signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());

            byte[] sigBytes = Base64.getDecoder().decode(signature);
            return sig.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
