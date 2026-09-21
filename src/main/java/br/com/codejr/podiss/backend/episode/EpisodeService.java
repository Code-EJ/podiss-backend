// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import br.com.codejr.podiss.backend.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
/**
 * Coordinates validated YouTube metadata retrieval and episode persistence. Duplicate provider IDs conflict; absent records are not found.
 *
 * @author oEnzoRibas
 */
@Service("videoService") @RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository repository;
    private final YouTubeClient youtube;
    public Page<Episode> list(Pageable pageable) { return repository.findAll(pageable); }
    public Episode get(String id) {
        if (!id.matches("[A-Za-z0-9_-]{11}"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "YouTube ID inválido.");
        return repository.findByYoutubeId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Episódio não encontrado."));
    }
    /**
     * Resolves remote metadata before opening the repository write transaction.
     * @param request validated URL and optional description override
     * @return persisted episode with canonical URL
     * @throws ApiException for invalid URL, duplicate episode or unavailable provider
     */
    public Episode create(CreateEpisodeRequest request) {
        String id = YouTubeUrl.id(request.videoUrl());
        if (repository.existsByYoutubeId(id)) throw new ApiException(HttpStatus.CONFLICT, "Episódio já cadastrado.");
        // No database transaction is held while waiting on the network.
        var metadata = youtube.fetch(id);
        Episode episode = new Episode();
        episode.setYoutubeId(id);
        episode.setVideoUrl(YouTubeUrl.canonical(id));
        episode.setTitle(metadata.title());
        episode.setDescription(request.description() == null ? metadata.description() : request.description().trim());
        episode.setThumbnailUrl(metadata.thumbnailUrl());
        episode.setCreatedAt(Timestamp.from(Instant.now()));
        return repository.saveAndFlush(episode);
    }
    public void delete(UUID id) {
        Episode episode = repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Episódio não encontrado."));
        repository.delete(episode);
    }
}
