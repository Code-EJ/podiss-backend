// src/main/java/com/code/yolanda/back/user/UserRepository.java
package com.code.yolanda.back.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
}
