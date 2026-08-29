// src/main/java/com/code/yolanda/back/user/LoginRequest.java
package br.com.codejr.podiss.backend.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
