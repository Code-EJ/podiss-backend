package br.com.codejr.podiss.backend;

import br.com.codejr.podiss.backend.contact.*;
import br.com.codejr.podiss.backend.suggestion.*;
import br.com.codejr.podiss.backend.episode.*;
import br.com.codejr.podiss.backend.post.*;
import br.com.codejr.podiss.backend.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Guards external names and persistence references while the fresh database adopts English physical names.
 * Uses an isolated H2 schema; Flyway and the production datasource are not used.
 *
 * @author oEnzoRibas
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:naming-contract;MODE=MariaDB;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.cors.allowed-origins=http://localhost:5173",
    "app.bootstrap.username=",
    "app.bootstrap.email=",
    "app.bootstrap.password="
})
@AutoConfigureMockMvc
@Transactional
class NamingCompatibilityTests {
    @Autowired ObjectMapper mapper;
    @Autowired ApplicationContext context;
    @Autowired EntityManager entities;
    @Autowired MockMvc http;
    @Autowired ContactMessageRepository contacts;
    @Autowired TopicSuggestionRepository suggestions;
    @Autowired EpisodeRepository episodes;
    @Autowired PostRepository posts;

    @Test
    void publicJsonPropertiesRemainUnchanged() {
        assertThat(properties(new ContactMessage()))
            .containsExactlyInAnyOrder("id", "nome", "email", "assunto", "mensagem", "createdAt");
        assertThat(properties(new TopicSuggestion()))
            .containsExactlyInAnyOrder("id", "nome", "email", "tema", "createdAt");
        assertThat(properties(new Episode()))
            .containsExactlyInAnyOrder("id", "title", "description", "youtubeId", "videoUrl", "thumbnailUrl", "createdAt");
    }

    @Test
    void requestRecordsKeepLegacyJsonNames() throws Exception {
        var contact = mapper.readValue("""
            {"nome":"Ana","email":"ana@example.com","assunto":"Contato","mensagem":"Mensagem"}
            """, CreateContactMessageRequest.class);
        assertThat(contact.nome()).isEqualTo("Ana");
        assertThat(contact.assunto()).isEqualTo("Contato");
        assertThat(contact.mensagem()).isEqualTo("Mensagem");
        var suggestion = mapper.readValue("""
            {"nome":"Ana","email":"ana@example.com","tema":"Tecnologia"}
            """, CreateTopicSuggestionRequest.class);
        assertThat(suggestion.tema()).isEqualTo("Tecnologia");
        var episode = mapper.readValue("""
            {"videoUrl":"https://youtu.be/abcdefghijk","description":"Descrição"}
            """, CreateEpisodeRequest.class);
        assertThat(episode.videoUrl()).isEqualTo("https://youtu.be/abcdefghijk");
        var post = mapper.readValue("""
            {"title":"Título","description":"Texto","tags":["java"]}
            """, CreatePostRequest.class);
        assertThat(post.getTags()).containsExactly("java");
    }

    @Test
    void logicalEntityNamesRemainStableAndPhysicalTablesUseEnglish() {
        assertThat(entities.getMetamodel().entity(ContactMessage.class).getName()).isEqualTo("Contato");
        assertThat(entities.getMetamodel().entity(TopicSuggestion.class).getName()).isEqualTo("Sugestao");
        assertThat(entities.getMetamodel().entity(Episode.class).getName()).isEqualTo("Video");
        assertThat(ContactMessage.class.getAnnotation(Table.class).name()).isEqualTo("contact_messages");
        assertThat(TopicSuggestion.class.getAnnotation(Table.class).name()).isEqualTo("topic_suggestions");
        assertThat(Episode.class.getAnnotation(Table.class).name()).isEqualTo("episodes");
    }

    @Test
    void existingBeanIdentifiersRemainResolvable() {
        assertThat(context.getBean("contatoController")).isInstanceOf(ContactMessageController.class);
        assertThat(context.getBean("contatoService")).isInstanceOf(ContactMessageService.class);
        assertThat(context.getBean("contatoRepository")).isInstanceOf(ContactMessageRepository.class);
        assertThat(context.getBean("sugestaoController")).isInstanceOf(TopicSuggestionController.class);
        assertThat(context.getBean("sugestaoService")).isInstanceOf(TopicSuggestionService.class);
        assertThat(context.getBean("sugestaoRepository")).isInstanceOf(TopicSuggestionRepository.class);
        assertThat(context.getBean("videoController")).isInstanceOf(EpisodeController.class);
        assertThat(context.getBean("videoService")).isInstanceOf(EpisodeService.class);
        assertThat(context.getBean("videoRepository")).isInstanceOf(EpisodeRepository.class);
        assertThat(context.getBean("jwtTokenUtil")).isInstanceOf(JwtTokenService.class);
    }

    @Test
    void repositoriesAndLegacyJpqlStillResolveRenamedEntities() {
        ContactMessage contact = new ContactMessage();
        contact.setSenderName("Ana"); contact.setEmail("ana@example.com");
        contact.setSubject("Assunto"); contact.setMessage("Mensagem");
        contact.setCreatedAt(Timestamp.from(Instant.now()));
        contacts.saveAndFlush(contact);
        assertThat(entities.createQuery("select c from Contato c where c.id = :id", ContactMessage.class)
            .setParameter("id", contact.getId()).getSingleResult().getSenderName()).isEqualTo("Ana");

        TopicSuggestion suggestion = new TopicSuggestion();
        suggestion.setSenderName("Ana"); suggestion.setEmail("ana@example.com");
        suggestion.setTopic("Tema"); suggestion.setCreatedAt(Timestamp.from(Instant.now()));
        suggestions.saveAndFlush(suggestion);
        assertThat(entities.createQuery("select s from Sugestao s where s.id = :id", TopicSuggestion.class)
            .setParameter("id", suggestion.getId()).getSingleResult().getTopic()).isEqualTo("Tema");

        Episode episode = new Episode();
        episode.setTitle("Episódio"); episode.setDescription("Descrição");
        episode.setYoutubeId("abcdefghijk"); episode.setVideoUrl("https://www.youtube.com/watch?v=abcdefghijk");
        episode.setCreatedAt(Timestamp.from(Instant.now()));
        episodes.saveAndFlush(episode);
        assertThat(episodes.existsByYoutubeId("abcdefghijk")).isTrue();
        assertThat(episodes.findByYoutubeId("abcdefghijk")).isPresent();
        assertThat(entities.createQuery("select v from Video v where v.id = :id", Episode.class)
            .setParameter("id", episode.getId()).getSingleResult().getTitle()).isEqualTo("Episódio");

        Post post = new Post();
        post.setTitle("Publicação"); post.setDescription("Texto");
        post.setCreatedAt(Timestamp.from(Instant.now()));
        posts.saveAndFlush(post);
        assertThat(posts.findSummaries(PageRequest.of(0, 10)).getContent())
            .anySatisfy(p -> assertThat(p.id()).isEqualTo(post.getId()));
    }

    @Test
    void publicSubmissionPathsAndPayloadsRemainUnchanged() throws Exception {
        http.perform(post("/contatos").contentType(MediaType.APPLICATION_JSON).content("""
            {"nome":"Ana","email":"ana@example.com","assunto":"Assunto","mensagem":"Mensagem"}
            """)).andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Ana"))
            .andExpect(jsonPath("$.mensagem").value("Mensagem")).andExpect(jsonPath("$.name").doesNotExist());
        http.perform(post("/sugestoes").contentType(MediaType.APPLICATION_JSON).content("""
            {"nome":"Ana","email":"ana@example.com","tema":"Tema"}
            """)).andExpect(status().isCreated()).andExpect(jsonPath("$.tema").value("Tema"));
        http.perform(get("/episodes")).andExpect(status().isOk())
            .andExpect(header().exists("X-Total-Pages")).andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void multipartBindingAndPostResponseRemainUnchanged() throws Exception {
        http.perform(multipart("/posts").param("title", "Título").param("description", "Texto").param("tags", "java"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Título"))
            .andExpect(jsonPath("$.tags").value("java")).andExpect(jsonPath("$.hasImage").value(false))
            .andExpect(jsonPath("$.image").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    void renamedControllersStillRequireAdmin() throws Exception {
        http.perform(get("/contatos")).andExpect(status().isForbidden());
        http.perform(get("/sugestoes")).andExpect(status().isForbidden());
        http.perform(post("/episodes").contentType(MediaType.APPLICATION_JSON)
            .content("{\"videoUrl\":\"https://youtu.be/abcdefghijk\"}")).andExpect(status().isForbidden());
    }

    private Set<String> properties(Object value) {
        Set<String> names = new HashSet<>();
        mapper.valueToTree(value).fieldNames().forEachRemaining(names::add);
        return names;
    }
}
