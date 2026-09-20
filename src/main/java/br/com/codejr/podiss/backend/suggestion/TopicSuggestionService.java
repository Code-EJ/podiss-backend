// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.suggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.sql.Timestamp;
import java.time.Instant;
@Service("sugestaoService") @RequiredArgsConstructor
public class TopicSuggestionService {
    private final TopicSuggestionRepository repository;
    @Transactional
    public TopicSuggestion create(CreateTopicSuggestionRequest request) {
        TopicSuggestion entity = new TopicSuggestion();
        entity.setSenderName(request.nome().trim());
        entity.setEmail(request.email().trim());
        entity.setTopic(request.tema().trim());
        entity.setCreatedAt(Timestamp.from(Instant.now()));
        return repository.saveAndFlush(entity);
    }
    public Page<TopicSuggestion> list(Pageable pageable) { return repository.findAll(pageable); }
}
