// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import br.com.codejr.podiss.backend.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
/**
 * Retrieves metadata only from fixed YouTube endpoints. Optional API credentials select the Data API; otherwise oEmbed is used.
 *
 * @author oEnzoRibas
 */
@Component
public class YouTubeClient {
    public record Metadata(String title, String description, String thumbnailUrl) {}
    private final RestClient client;
    private final String apiKey;
    public YouTubeClient(RestClient youtubeRestClient, @Value("${app.youtube.api-key:}") String apiKey) {
        this.client = youtubeRestClient; this.apiKey = apiKey;
    }
    public Metadata fetch(String id) {
        URI endpoint = apiKey.isBlank()
            ? UriComponentsBuilder.fromUriString("https://www.youtube.com/oembed")
                .queryParam("url", YouTubeUrl.canonical(id)).queryParam("format", "json").build().encode().toUri()
            : UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("part", "snippet").queryParam("id", id).queryParam("key", apiKey).build().encode().toUri();
        try {
            JsonNode body = client.get().uri(endpoint).retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
                    int code = response.getStatusCode().value();
                    if (code == 404 || code == 400 || (apiKey.isBlank() && (code == 401 || code == 403)))
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Vídeo indisponível, privado ou sem incorporação permitida.");
                    throw new ApiException(HttpStatus.BAD_GATEWAY, "Falha ao consultar o YouTube. Tente novamente mais tarde.");
                }).body(JsonNode.class);
            if (body == null) throw new ApiException(HttpStatus.BAD_GATEWAY, "Resposta vazia do YouTube.");
            String title, description, thumbnail;
            if (apiKey.isBlank()) {
                title = body.path("title").asText("");
                description = "";
                thumbnail = body.path("thumbnail_url").asText("");
            } else {
                if (!body.path("items").isArray()) throw new ApiException(HttpStatus.BAD_GATEWAY, "Resposta inválida do YouTube.");
                if (body.path("items").isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Vídeo não encontrado.");
                JsonNode snippet = body.path("items").get(0).path("snippet");
                title = snippet.path("title").asText("");
                description = snippet.path("description").asText("");
                thumbnail = snippet.path("thumbnails").path("high").path("url").asText("");
            }
            if (title.isBlank() || title.length() > 255 || description.length() > 10000 || thumbnail.length() > 512)
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Metadados inválidos retornados pelo YouTube.");
            return new Metadata(title, description, thumbnail.isBlank() ? null : thumbnail);
        } catch (ResourceAccessException e) {
            for (Throwable cause = e; cause != null; cause = cause.getCause())
                if (cause instanceof HttpTimeoutException || cause instanceof SocketTimeoutException)
                    throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "Tempo limite ao consultar o YouTube.");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Não foi possível conectar ao YouTube.");
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Resposta inválida do YouTube.");
        }
    }
}
