// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
/**
 * Stored account with BCrypt password hash and explicit role. This entity must not be returned as a public HTTP response.
 *
 * @author oEnzoRibas
 */
@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) @Column(name = "id")
    private UUID id;
    @Column(name = "username", unique = true, nullable = false, length = 255)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false, length = 16)
    private Role role = Role.USER;
    public enum Role { USER, ADMIN }
}
