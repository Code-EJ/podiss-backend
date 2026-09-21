// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import br.com.codejr.podiss.backend.security.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
/**
 * Authenticates credentials and issues tokens. Registration is an administrator operation, not public self-registration.
 *
 * @author oEnzoRibas
 */
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokens;
    private final UserService users;
    @PostMapping("/register") @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Register account", description = "ADMIN creates account; omitted role defaults to USER. Returns a success string.")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        users.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }
    @PostMapping("/login")
    @io.swagger.v3.oas.annotations.Operation(summary = "Authenticate", description = "Returns token for Authorization: Bearer requests; passwords are never returned.")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return new JwtResponse(tokens.generateToken((UserDetails) authentication.getPrincipal()));
    }
}
