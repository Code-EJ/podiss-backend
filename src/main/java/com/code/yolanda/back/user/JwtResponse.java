// src/main/java/com/code/yolanda/back/user/JwtResponse.java
package com.code.yolanda.back.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
}
