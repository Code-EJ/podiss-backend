package br.com.codejr.podiss.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Documentation stays inaccessible even to administrators when disabled.
 * @author oEnzoRibas
 */
@SpringBootTest(properties = {
    "springdoc.api-docs.enabled=false", "springdoc.swagger-ui.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:openapi-disabled;MODE=MariaDB;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false", "app.bootstrap.username=", "app.bootstrap.email=", "app.bootstrap.password="
})
@AutoConfigureMockMvc
class OpenApiDisabledTests {
    @Autowired MockMvc http;
    @Test @WithMockUser(roles = "ADMIN")
    void documentationIsDenied() throws Exception {
        for (String path : new String[]{"/v3/api-docs", "/v3/api-docs.yaml", "/swagger-ui/index.html"})
            http.perform(get(path)).andExpect(status().isForbidden());
    }
}
