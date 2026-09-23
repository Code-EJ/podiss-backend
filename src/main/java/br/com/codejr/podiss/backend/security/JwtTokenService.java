// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
/**
 * Issues HS512 tokens and verifies signature, issuer, expiration and subject. Immutable signing configuration is shared across requests; tokens have no individual revocation store.
 *
 * @author oEnzoRibas
 */
@Component("jwtTokenUtil")
public class JwtTokenService {
    private final SecretKey key;
    private final String issuer;
    private final Duration ttl;
    public JwtTokenService(@Value("${app.jwt.secret}") String secret,
                        @Value("${app.jwt.issuer}") String issuer,
                        @Value("${app.jwt.ttl}") Duration ttl) {
        byte[] decoded;
        try { decoded = Decoders.BASE64.decode(secret); }
        catch (RuntimeException e) { throw new IllegalArgumentException("JWT_SECRET deve ser Base64.", e); }
        if (decoded.length < 64) throw new IllegalArgumentException("JWT_SECRET deve conter pelo menos 64 bytes aleatórios em Base64.");
        if (ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("JWT TTL deve ser positivo.");
        this.key = Keys.hmacShaKeyFor(decoded);
        this.issuer = issuer;
        this.ttl = ttl;
    }
    /**
     * Verifies a token before returning its subject; no database lookup occurs here.
     * @param token compact signed JWT, without the Bearer prefix
     * @return nonblank username from verified claims
     * @throws JwtException if signature, issuer, expiration or required claims are invalid
     * @throws IllegalArgumentException if the supplied token is empty
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
            .parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank())
            throw new MalformedJwtException("Token sem expiração ou usuário.");
        return claims.getSubject();
    }
    public String generateToken(UserDetails user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().issuer(issuer).subject(user.getUsername())
            .issuedAt(new Date(now)).expiration(new Date(now + ttl.toMillis()))
            .signWith(key, Jwts.SIG.HS512).compact();
    }
}
