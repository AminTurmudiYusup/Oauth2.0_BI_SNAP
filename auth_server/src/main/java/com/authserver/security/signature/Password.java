package com.authserver.security.signature;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Password {
    public static void main(String[] args) {
        String hash = new BCryptPasswordEncoder().encode("password123");
        System.out.println(hash);
    }
}
