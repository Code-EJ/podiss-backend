// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;
@Configuration
public class YouTubeConfig {
    @Bean
    RestClient youtubeRestClient(RestClient.Builder builder,
            @Value("${app.youtube.connect-timeout}") Duration connect,
            @Value("${app.youtube.read-timeout}") Duration read) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(connect)
            .followRedirects(HttpClient.Redirect.NEVER).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(read);
        return builder.requestFactory(factory).build();
    }
}
