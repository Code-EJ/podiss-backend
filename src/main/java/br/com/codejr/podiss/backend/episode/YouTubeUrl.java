// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import br.com.codejr.podiss.backend.common.ApiException;
import org.springframework.http.HttpStatus;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class YouTubeUrl {
    private YouTubeUrl() {}
    public static String id(String value) {
        try {
            URI uri = URI.create(value);
            if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getUserInfo() != null || uri.getPort() != -1)
                throw new IllegalArgumentException();
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.getPath();
            String id = null;
            if (host.equals("youtu.be")) {
                id = path.substring(1);
            } else if (Set.of("youtube.com", "www.youtube.com", "m.youtube.com").contains(host)) {
                if (path.equals("/watch") && uri.getRawQuery() != null) {
                    for (String part : uri.getRawQuery().split("&")) {
                        String[] pair = part.split("=", 2);
                        if (pair.length == 2 && pair[0].equals("v")) {
                            if (id != null) throw new IllegalArgumentException();
                            id = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        }
                    }
                } else {
                    for (String prefix : List.of("/embed/", "/shorts/", "/live/"))
                        if (path.startsWith(prefix)) id = path.substring(prefix.length());
                }
            }
            if (id == null || !id.matches("[A-Za-z0-9_-]{11}")) throw new IllegalArgumentException();
            return id;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe uma URL válida de vídeo do YouTube.");
        }
    }
    public static String canonical(String id) { return "https://www.youtube.com/watch?v=" + id; }
}
