// src/main/java/com/code/yolanda/back/user/LoginRequest.java
package com.code.yolanda.back.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
