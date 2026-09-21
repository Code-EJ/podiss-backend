package br.com.codejr.podiss.backend;

import br.com.codejr.podiss.backend.common.ApiException;
import br.com.codejr.podiss.backend.episode.YouTubeUrl;
import br.com.codejr.podiss.backend.post.ImageValidator;
import br.com.codejr.podiss.backend.security.JwtTokenService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import java.time.Duration;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

/** Local regression guards for security boundaries; not an exploit or penetration test.
 * @author oEnzoRibas
 */
class SecurityBoundaryTests {
    private final String key = Base64.getEncoder().encodeToString(new byte[64]);

    @Test void jwtRejectsWeakKeysAndInvalidLifetime() {
        assertThatThrownBy(() -> new JwtTokenService("YWJj", "test", Duration.ofHours(1)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtTokenService(key, "test", Duration.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }
    @Test void tokenRequiresItsIssuerAndSignature() {
        var service = new JwtTokenService(key, "test", Duration.ofHours(1));
        var user = User.withUsername("local-test").password("unused").roles("USER").build();
        String token = service.generateToken(user);
        assertThat(service.getUsernameFromToken(token)).isEqualTo("local-test");
        assertThatThrownBy(() -> new JwtTokenService(key, "other", Duration.ofHours(1)).getUsernameFromToken(token))
            .isInstanceOf(JwtException.class);
        byte[] other = new byte[64]; other[0] = 1;
        assertThatThrownBy(() -> new JwtTokenService(Base64.getEncoder().encodeToString(other), "test",
            Duration.ofHours(1)).getUsernameFromToken(token)).isInstanceOf(JwtException.class);
    }
    @Test void youtubeRejectsArbitraryDestinations() {
        for (String url : new String[]{"https://example.com/watch?v=abcdefghijk",
                "https://youtube.com@127.0.0.1/watch?v=abcdefghijk",
                "https://youtube.com:8443/watch?v=abcdefghijk",
                "https://youtube.com/watch?v=abcdefghijk&v=abcdefghijk"})
            assertThatThrownBy(() -> YouTubeUrl.id(url)).isInstanceOf(ApiException.class);
        assertThat(YouTubeUrl.id("https://youtu.be/abcdefghijk")).isEqualTo("abcdefghijk");
    }
    @Test void imageDoesNotTrustClientMimeType() {
        assertThatThrownBy(() -> new ImageValidator().read(new MockMultipartFile(
            "image", "fake.png", "image/png", "<script>alert(1)</script>".getBytes(java.nio.charset.StandardCharsets.UTF_8))))
            .isInstanceOf(ApiException.class);
    }
    @Test void imageRejectsOversizedFiles() {
        assertThatThrownBy(() -> new ImageValidator().read(new MockMultipartFile(
            "image", new byte[5 * 1024 * 1024 + 1]))).isInstanceOf(ApiException.class);
    }
}
