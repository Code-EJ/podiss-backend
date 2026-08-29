// src/main/java/com/code/yolanda/back/user/JwtResponse.java
package br.com.codejr.podiss.backend.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
}
