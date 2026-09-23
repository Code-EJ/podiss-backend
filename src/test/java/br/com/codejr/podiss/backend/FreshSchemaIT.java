package br.com.codejr.podiss.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Fresh installation only, on a disposable local MariaDB container.
 * Inherits all HTTP/JPA compatibility tests; never accepts an external DB URL.
 * Docker is required: unavailable infrastructure fails rather than skipping silently.
 *
 * @author oEnzoRibas
 */
@Testcontainers
class FreshSchemaIT extends NamingCompatibilityTests {
    @Container
    static final MariaDBContainer<?> DATABASE = new MariaDBContainer<>("mariadb:11.8.9")
        .withDatabaseName("podiss_fresh_test")
        .withUsername("podiss_test")
        .withPassword("local_test_only");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", DATABASE::getJdbcUrl);
        properties.add("spring.datasource.username", DATABASE::getUsername);
        properties.add("spring.datasource.password", DATABASE::getPassword);
        properties.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.jpa.open-in-view", () -> "false");
        properties.add("spring.flyway.enabled", () -> "true");
        properties.add("spring.flyway.locations", () -> "classpath:db/fresh-migration");
        properties.add("spring.flyway.baseline-on-migrate", () -> "false");
        properties.add("spring.flyway.clean-disabled", () -> "true");
    }

    @Autowired Flyway flyway;
    @Autowired JdbcTemplate sql;

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void registeredUserRoleAndPasswordRoundTripOnMariaDb() throws Exception {
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/register")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("""
                {"username":"fresh_admin","email":"fresh@example.com","password":"Local_test_password_123","role":"ADMIN"}
                """))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        entities.flush();
        entities.clear();
        assertThat(sql.queryForObject("SELECT role FROM users WHERE username = 'fresh_admin'", String.class)).isEqualTo("ADMIN");
        assertThat(sql.queryForObject("SELECT password FROM users WHERE username = 'fresh_admin'", String.class))
            .isNotEqualTo("Local_test_password_123");
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("""
                {"username":"fresh_admin","password":"Local_test_password_123"}
                """))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void unicodeAndImageBytesSurviveJpaRoundTrip() {
        var post = new br.com.codejr.podiss.backend.post.Post();
        post.setTitle("Publicação 🎙️");
        post.setDescription("Ação, café e tecnologia");
        post.setCreatedAt(java.sql.Timestamp.from(java.time.Instant.parse("2026-09-20T12:00:00.123456Z")));
        post.setImage(new byte[] {0, 1, -1, 42});
        post.setImageContentType("application/octet-stream");
        posts.saveAndFlush(post);
        entities.clear();
        var loaded = posts.findById(post.getId()).orElseThrow();
        assertThat(loaded.getTitle()).isEqualTo(post.getTitle());
        assertThat(loaded.getImage()).containsExactly(0, 1, -1, 42);
        assertThat(loaded.getImageContentType()).isEqualTo("application/octet-stream");
        assertThat(loaded.getCreatedAt()).isEqualTo(post.getCreatedAt());
        assertThat(loaded.getTags()).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void onlyFreshMigrationIsDiscoveredAndSecondRunDoesNotChangeData() {
        var migrations = flyway.info().all();
        assertThat(migrations).hasSize(1);
        assertThat(migrations[0].getScript()).isEqualTo("V1__fresh_schema.sql");
        assertThat(migrations[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        String id = UUID.randomUUID().toString();
        sql.update("INSERT INTO contact_messages VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6))",
            id, "Ana", "ana@example.com", "Assunto", "Mensagem");
        try {
            assertThat(flyway.migrate().migrationsExecuted).isZero();
            assertThat(sql.queryForObject("SELECT message FROM contact_messages WHERE id = ?", String.class, id))
                .isEqualTo("Mensagem");
            assertThat(sql.queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()", String.class))
                .containsExactlyInAnyOrder("users", "posts", "episodes", "contact_messages", "topic_suggestions", "flyway_schema_history");
        } finally {
            sql.update("DELETE FROM contact_messages WHERE id = ?", id);
        }
    }

    @Test
    void constraintsIndexesAndCaseSensitiveYoutubeIdsArePresent() {
        sql.update("INSERT INTO episodes (id,title,description,youtube_id,video_url,created_at) VALUES (?, 'A', '', 'Abcdefghijk', 'https://youtu.be/Abcdefghijk', CURRENT_TIMESTAMP(6))", UUID.randomUUID().toString());
        sql.update("INSERT INTO episodes (id,title,description,youtube_id,video_url,created_at) VALUES (?, 'B', '', 'abcdefghijk', 'https://youtu.be/abcdefghijk', CURRENT_TIMESTAMP(6))", UUID.randomUUID().toString());
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM episodes WHERE youtube_id = 'Abcdefghijk'", Integer.class)).isEqualTo(1);
        assertThatThrownBy(() -> sql.update("INSERT INTO episodes (id,title,description,youtube_id,video_url,created_at) VALUES (?, 'C', '', 'Abcdefghijk', 'x', CURRENT_TIMESTAMP(6))", UUID.randomUUID().toString()))
            .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(sql.queryForList("SELECT DISTINCT index_name FROM information_schema.statistics WHERE table_schema = DATABASE()", String.class))
            .contains("uk_users_username", "uk_users_email", "uk_episodes_youtube_id",
                "idx_posts_created_at_id", "idx_episodes_created_at_id",
                "idx_contact_messages_created_at_id", "idx_topic_suggestions_created_at_id");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void nonEmptyLegacyDatabaseIsRejectedWithoutChangingItsRows() throws Exception {
        // Separate disposable container; no host-provided credentials or database names.
        try (var legacy = new MariaDBContainer<>("mariadb:11.8.9")
                .withDatabaseName("legacy_guard").withUsername("test").withPassword("test")) {
            legacy.start();
            try (var connection = DriverManager.getConnection(legacy.getJdbcUrl(), legacy.getUsername(), legacy.getPassword());
                 var statement = connection.createStatement()) {
                statement.execute("CREATE TABLE legacy_marker (id INT PRIMARY KEY)");
                statement.execute("INSERT INTO legacy_marker VALUES (42)");
                var candidate = Flyway.configure().dataSource(legacy.getJdbcUrl(), legacy.getUsername(), legacy.getPassword())
                    .locations("classpath:db/fresh-migration").baselineOnMigrate(false).cleanDisabled(true).load();
                assertThatThrownBy(candidate::migrate).isInstanceOf(org.flywaydb.core.api.FlywayException.class);
                try (var rows = statement.executeQuery("SELECT id FROM legacy_marker")) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getInt(1)).isEqualTo(42);
                }
                try (var tables = statement.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('users','contact_messages','topic_suggestions')")) {
                    tables.next();
                    assertThat(tables.getInt(1)).isZero();
                }
            }
        }
    }
}
