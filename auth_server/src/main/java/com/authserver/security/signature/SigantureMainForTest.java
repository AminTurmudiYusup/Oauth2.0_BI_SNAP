package com.authserver.security.signature;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SigantureMainForTest {
    public static void main(String[] args) throws Exception {
        String clientId = "my-client-id";
        String timestamp = "20260406T120000";
        String message = clientId + "|" + timestamp;

        // 1. Generate keys
//        KeyPair keyPair = generateKeyPair();
//
//        String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
//        String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
//
//        System.out.println("PUBLIC KEY (Base64):");
//        System.out.println(publicKeyStr);
//
//        System.out.println("\nPRIVATE KEY (Base64):");
//        System.out.println(privateKeyStr);
        // 2. Sign message
        String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApEybuce1TD6W11HeDzQU5Q0Z4YSjaQA2jVSxw4oJDqcsI1dPbNKR13KKuRVTE0KMqVupHJwKtwoKXshN7xZnBEk+XTNOaULvkkYb1QrassFGdw2tMHDU+P9wz/VBJcCxW9tK+d8cnxb1l4gCELs7pT6zBMBJFhDGJgTeaytzAlou0c2aAZAe5oEbb/9IlyULNbRjMvDsIFG8jpjOIkMA//Qml0KBnDgKeEuUQ1aR4/VE0cI+tBVVbnBNKCSgCvjxfE+Vtyq3WDaO5V1L7+VE8BCeDu33BcGEcP7QO/EbtK1Pjg/CKIFBmHTtKt8hl4ZnGpffnpdd3EQv7lR5883RpQIDAQAB";
        String privateKeyStr = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCkTJu5x7VMPpbXUd4PNBTlDRnhhKNpADaNVLHDigkOpywjV09s0pHXcoq5FVMTQoypW6kcnAq3CgpeyE3vFmcEST5dM05pQu+SRhvVCtqywUZ3Da0wcNT4/3DP9UElwLFb20r53xyfFvWXiAIQuzulPrMEwEkWEMYmBN5rK3MCWi7RzZoBkB7mgRtv/0iXJQs1tGMy8OwgUbyOmM4iQwD/9CaXQoGcOAp4S5RDVpHj9UTRwj60FVVucE0oJKAK+PF8T5W3KrdYNo7lXUvv5UTwEJ4O7fcFwYRw/tA78Ru0rU+OD8IogUGYdO0q3yGXhmcal9+el13cRC/uVHnzzdGlAgMBAAECggEAHIjJiuxqSkGchOzM+ir7bgRmeEpG7xeAYe9RqDoy9H6IJeMmAipX9ekIurg/Y1rtfwRSTIu1OLDfuDJlhp6HabEmyZz9gz6CyQ6wCsIF1YHHsSmI6J7P/iau+w8HL31V0DQ3w0nCeMbOBHE0mp9J7psDEFIs304ue6/gyRAUu1WSwqXDJoJfu+uKgvzeVHQfvxq4Nxq8pokxhQasZG720h/KPb1+He42eZ54pX621x8In5NF5piJtB83/tC0Ppc1b/j4BdCvmvg2+8oZOwkuEzdDv8KZStYc5GU9xZhpiHoqGwebWeg3nNbUjsNel6u/8Y7J+vm1u7YI/EIYlI3WCQKBgQC7knDoih08gQ+tsFte/bv4PcZamoE9m+4EprvTAznsEME8S/jRWQz0fT3lf6PaZ3Xs/TPFgEHcnmDxF54vgEPGW8z9tEqMDlJV9tl22zWGM4R1E+J95/0t/8eqmehVy9Y8PrIgATqXSiGiK2OgrPTXpbg77Gewh/8Ipi2+x9LKXwKBgQDgPLJBisHenG3SASZhIpltOOuTJeMozFJDe+YgKZ77oRgY0UEOEMeS263FtOm5ShLwZmUOPC7fC4HW6cdfJi6q8WdJ/hdr7/lSE8gjmJ0BzDdvT72VVWEzWAWSVrpjBCbc2nXqc9jqssNcJVCmI5mH9bv6YlkLt5k2KisH2o0qewKBgDiF75pTjG3gIn5fp5kfcxZiXzSMXKfZJkemivEShtDZV7G43xkO8bV3TECtTw4QoVuyDXjVpoM6AtKEIHoF9+Z9YW+OdexwoGUhRvTnJ+DyA1dYj6cLUqMa8es6B+Zaz4tGBV1ZoXNXtnztAE+57upRGQyNt81dnf/YO+7CvoWXAoGADJE6SFQXg3tu6DsqRH1k7esnwMp5zbqNzbxu7FSI1X9000c/ByPHm6J2PcNi6gCNyS1j8oVK9zM05q+Zfeqs6OW4rTvxYhpD9ean697MxeIFJVbpwCnV1cPUJBx6O7xq1SvlOeueN6EnM83flEAgha8vBtxmMzLqAlAgs0L7cGsCgYEAr6xi/skhhEUU/h85eh5d+kBVR27MON+3t4j2OryEd9Cb+CGfWGHVD6iDE3TdEnvAOi1AkAN/TftQwW3UUQiLxfEq57K8qVIHEjkaTNxDQKtavAVjKcVjrc2UwauaRgV3939H29DcnkJUKxT983pOK8qs9koonHednDCmacIkB5w=";
        byte[] keyBytesPrivate = Base64.getDecoder().decode(privateKeyStr);
        byte[] keyBytesPublic = Base64.getDecoder().decode(publicKeyStr);
        String signature = sign(message, getPrivate(keyBytesPrivate));
        System.out.println("Signature: " + signature);

        // 3. Verify signature
        boolean isValid = verify(message, signature, getPublic(keyBytesPublic));
        System.out.println("Valid: " + isValid);
    }

    // Generate RSA key pair
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    // Sign data with private key
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());

        byte[] signedBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signedBytes);
    }

    // Verify signature with public key
    public static boolean verify(String data, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());

        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }

    public static PrivateKey getPrivate(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC", "DSA"
        return kf.generatePrivate(spec);
    }

    public static PublicKey getPublic(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC", "DSA"
        return kf.generatePublic(spec);
    }
}
