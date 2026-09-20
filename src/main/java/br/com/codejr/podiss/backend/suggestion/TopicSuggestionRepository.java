package br.com.codejr.podiss.backend.suggestion;// src/main/java/com/code/yolanda/back/sugestao/SugestaoRepository.java


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

@org.springframework.stereotype.Repository("sugestaoRepository")
public interface TopicSuggestionRepository extends JpaRepository<TopicSuggestion, UUID> {
}
