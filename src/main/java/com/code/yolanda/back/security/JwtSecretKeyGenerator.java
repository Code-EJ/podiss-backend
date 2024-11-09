package com.code.yolanda.back.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
// Fiz ele pra criptografar as senhas de registro
public class JwtSecretKeyGenerator {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64EncodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Sua chave secreta Base64:");
        System.out.println(base64EncodedKey);
    }
}
