// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository repository;
    private final UserService users;
    private final Validator validator;
    @Value("${app.bootstrap.username:}") private String username;
    @Value("${app.bootstrap.email:}") private String email;
    @Value("${app.bootstrap.password:}") private String password;
    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() && email.isBlank() && password.isBlank()) return;
        RegisterRequest request = new RegisterRequest(username, password, email, User.Role.ADMIN);
        if (!validator.validate(request).isEmpty())
            throw new IllegalStateException("Configure ADMIN_USERNAME, ADMIN_EMAIL e ADMIN_PASSWORD (12 a 72 caracteres).");
        if (repository.existsByRole(User.Role.ADMIN)) return;
        // Do not elevate or overwrite an existing account silently.
        if (repository.existsByUsername(username) || repository.existsByEmail(email.trim().toLowerCase(java.util.Locale.ROOT)))
            throw new IllegalStateException("Bootstrap em conflito com usuário existente. Use um novo usuário administrador.");
        users.register(request);
    }
}
