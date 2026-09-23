package br.com.codejr.podiss.backend.suggestion;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * UUID-based suggestion persistence retaining its legacy bean alias for Spring compatibility.
 *
 * @author oEnzoRibas
 */
@org.springframework.stereotype.Repository("sugestaoRepository")
public interface TopicSuggestionRepository extends JpaRepository<TopicSuggestion, UUID> {
}
