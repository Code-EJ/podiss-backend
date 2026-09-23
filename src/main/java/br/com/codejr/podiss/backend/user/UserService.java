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
/**
 * Creates accounts with normalized email and BCrypt hashes, and loads current authorities for authentication. HTTP callers must enforce administrator authorization on registration.
 *
 * @author oEnzoRibas
 */
@Service @RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    /**
     * Persists an account and hashes its password; callers must validate the DTO and authorization.
     * @param request validated account fields, with an optional role
     * @return persisted account; never serialize its password hash to clients
     * @throws ApiException for known duplicates or passwords exceeding 72 UTF-8 bytes
     * @throws org.springframework.dao.DataIntegrityViolationException for concurrent uniqueness conflicts
     */
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
