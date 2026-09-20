// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
@Component("jwtTokenUtil")
public class JwtTokenService {
    private final Key key;
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
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).requireIssuer(issuer).build()
            .parseClaimsJws(token).getBody();
        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank())
            throw new MalformedJwtException("Token sem expiração ou usuário.");
        return claims.getSubject();
    }
    public String generateToken(UserDetails user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().setIssuer(issuer).setSubject(user.getUsername())
            .setIssuedAt(new Date(now)).setExpiration(new Date(now + ttl.toMillis()))
            .signWith(key, SignatureAlgorithm.HS512).compact();
    }
}
