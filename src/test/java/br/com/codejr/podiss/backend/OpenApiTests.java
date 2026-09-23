package br.com.codejr.podiss.backend;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifies generated HTTP documentation against the current controller inventory.
 * @author oEnzoRibas
 */
@SpringBootTest(properties = {
    "springdoc.api-docs.enabled=true", "springdoc.swagger-ui.enabled=true",
    "spring.datasource.url=jdbc:h2:mem:openapi;MODE=MariaDB;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false", "app.bootstrap.username=", "app.bootstrap.email=", "app.bootstrap.password="
})
@AutoConfigureMockMvc
class OpenApiTests {
    @Autowired MockMvc http;
    @Autowired ObjectMapper mapper;

    @Test
    void allOperationsHaveContractsAndCorrectAuthorization() throws Exception {
        JsonNode spec = mapper.readTree(http.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        Map<String, Set<String>> expected = Map.of(
            "/contatos", Set.of("get", "post"), "/sugestoes", Set.of("get", "post"),
            "/episodes", Set.of("get", "post"),
            "/episodes/{id}", Set.of("get", "delete"), "/posts", Set.of("get", "post"),
            "/posts/{id}", Set.of("get", "put", "delete"), "/posts/{id}/image", Set.of("put", "delete"),
            "/posts/image/{id}", Set.of("get"), "/api/auth/login", Set.of("post"));
        Set<String> paths = new HashSet<>(); spec.path("paths").fieldNames().forEachRemaining(paths::add);
        Set<String> all = new HashSet<>(expected.keySet()); all.add("/api/auth/register");
        assertThat(paths).isEqualTo(all);
        int operations = 0;
        for (String path : paths) {
            JsonNode item = spec.path("paths").path(path);
            Set<String> verbs = new HashSet<>(); item.fieldNames().forEachRemaining(verbs::add);
            assertThat(verbs).isEqualTo(expected.getOrDefault(path, Set.of("post")));
            for (String verb : verbs) {
                operations++;
                JsonNode operation = item.path(verb);
                assertThat(operation.path("summary").asText()).isNotBlank();
                assertThat(operation.path("description").asText()).isNotBlank();
                assertThat(operation.path("operationId").asText()).isNotBlank();
                boolean publicRoute = (verb.equals("get") && (path.startsWith("/posts") || path.startsWith("/episodes")))
                    || (verb.equals("post") && Set.of("/api/auth/login", "/contatos", "/sugestoes").contains(path));
                if (publicRoute) assertThat(operation.path("security").size()).isZero();
                else assertThat(operation.path("security").get(0).has("bearerAuth")).isTrue();
                String success = verb.equals("delete") ? "204" : verb.equals("post") && !path.endsWith("/login") ? "201" : "200";
                assertThat(operation.path("responses").has(success)).isTrue();
            }
        }
        assertThat(operations).isEqualTo(18);
        assertThat(spec.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(spec.at("/components/schemas/CreateContactMessageRequest/properties").has("nome")).isTrue();
        assertThat(spec.at("/components/schemas/CreateTopicSuggestionRequest/properties").has("tema")).isTrue();
        assertThat(spec.at("/paths/~1posts/get/responses/200/headers").size()).isEqualTo(4);
        assertThat(spec.at("/paths/~1posts/post/requestBody/content").has("multipart/form-data")).isTrue();
        assertThat(spec.at("/paths/~1posts~1image~1{id}/get/responses/200/content").has("image/png")).isTrue();
    }

    @Test
    void swaggerAssetsAndConfigurationAreAvailableWhenEnabled() throws Exception {
        http.perform(get("/swagger-ui/index.html")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
        http.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }
}
