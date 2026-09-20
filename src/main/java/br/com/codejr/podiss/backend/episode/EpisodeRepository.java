// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
@org.springframework.stereotype.Repository("videoRepository")
public interface EpisodeRepository extends JpaRepository<Episode, UUID> {
    Optional<Episode> findByYoutubeId(String youtubeId);
    boolean existsByYoutubeId(String youtubeId);
}
