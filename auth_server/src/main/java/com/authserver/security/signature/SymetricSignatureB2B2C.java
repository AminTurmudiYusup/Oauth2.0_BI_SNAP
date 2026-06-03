package com.authserver.security.signature;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class SymetricSignatureB2B2C {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Generate symmetric key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
        return keyGen.generateKey();
    }

    // Create signature (HMAC)
    public static String sign(String message, SecretKey key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(key);

        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    // Verify signature
    public static boolean verify(String message, String signature, SecretKey key) throws Exception {
        String computed = sign(message, key);

        // Decode both signatures to raw bytes
        byte[] computedBytes = Base64.getDecoder().decode(computed);
        byte[] providedBytes = Base64.getDecoder().decode(signature);

        // Constant-time comparison on REAL bytes
        return MessageDigest.isEqual(computedBytes, providedBytes);
    }

    // Convert SecretKey -> Base64 string
    public static String keyToBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Convert Base64 string -> SecretKey
    public static SecretKey base64ToKey(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decoded, "HmacSHA512");
    }

    // Main function
    public static String hashRequestBody(String requestBody) throws Exception {
        String minified = minify(requestBody);
        byte[] hash = sha256(minified);
        return bytesToHex(hash).toLowerCase();
    }

    // 2. SHA-256 hash
    public static byte[] sha256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data.getBytes(StandardCharsets.UTF_8));
    }

    // 3. Convert bytes to HEX
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b)); // already lowercase
        }
        return hex.toString();
    }

    public static void main(String[] args) throws Exception {
        String httpMethod = "POST";
        String urlRelative = "/api/v1/b2b2c/topup";
        String accessToken = "\"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhZjkwY2M0ZS04YjkxLTRmOGYtYWIwMS0wYTlmZDg5Yzk4MTAiLCJjbGllbnRJZCI6Im15LWNsaWVudC1pZCIsInNjb3BlIjoiQjJCIiwiaWF0IjoxNzgwNDc5OTI4LCJuYmYiOjE3ODA0Nzk5MjgsImV4cCI6MTc4MDQ4MzUyOH0.7bluWgq4S8P4JvlIQuUxh5io_wKekUj7cz0lmb9gAe8";
        String body = "{\n" +
                "  \"partnerId\": \"PARTNER_ABC\",\n" +
                "  \"accountId\": \"ACC123456\",\n" +
                "  \"amount\": 100.00,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"requestId\": \"REQ-001\",\n" +
                "  \"timestamp\": 1714900000000,\n" +
                "  \"signature\": \"dummy-signature\"\n" +
                "}";
        String timeStamp = "2020-12-17T10:55:00+07:00";
        String hashedBody = hashRequestBody(body);
        String stringToSign = httpMethod + ":" +
                urlRelative + ":" +
                accessToken + ":" +
                hashedBody + ":" +
                timeStamp;
        System.out.println(stringToSign);

        // 1. Generate key
        //SecretKey originalKey = generateKey();

        // 2. Convert key to Base64 (store/send this)
        // String base64Key = keyToBase64(originalKey);
        //System.out.println("Base64 Key: " + base64Key);

        // 3. Reconstruct key from Base64
        SecretKey restoredKey = base64ToKey("Ye4pKs6wKBQ9lh4GvkAurBZhILGCHRUF8t1wPrbGje9biKqB4XFuu0xiFvzcpVCve51uAezpx7nrRWLnzEIj6w==");

        // 2. Sign message
        String signature = sign(stringToSign, restoredKey);
        System.out.println("Signature: " + signature);

        // 3. Verify
        boolean isValid = verify(stringToSign, signature, restoredKey);
        System.out.println("Valid: " + isValid);
    }

    // 1. Minify (simple version)
    private static String minify(String body) {
        try {
            if (body == null || body.isBlank()) {
                return "";
            }
            Object json = objectMapper.readValue(body, Object.class);
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            return body.trim();
        }
    }
}
