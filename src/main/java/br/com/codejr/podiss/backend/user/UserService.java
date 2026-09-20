// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import br.com.codejr.podiss.backend.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
@Service @RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    @Transactional
    public User register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByUsername(request.username()) || repository.existsByEmail(email))
            throw new ApiException(HttpStatus.CONFLICT, "Usuário ou email já cadastrado.");
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ApiException(HttpStatus.BAD_REQUEST, "A senha deve ter no máximo 72 bytes em UTF-8.");
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setRole(request.role() == null ? User.Role.USER : request.role());
        return repository.saveAndFlush(user);
    }
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = repository.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("Usuário não encontrado.");
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
            .password(user.getPassword()).roles(user.getRole().name()).build();
    }
}
