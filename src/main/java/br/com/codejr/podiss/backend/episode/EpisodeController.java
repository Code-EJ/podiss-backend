// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import br.com.codejr.podiss.backend.common.PaginationSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/**
 * Publishes episode metadata with public reads and administrator-only writes. Lookup and deletion intentionally use different identifiers.
 *
 * @author oEnzoRibas
 */
@RestController("videoController") @RequestMapping("/episodes") @RequiredArgsConstructor
public class EpisodeController {
    private final EpisodeService service;
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List episodes", description = "Returns a paginated array of published episode metadata.")
    public ResponseEntity<List<Episode>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size, @RequestParam(defaultValue = "asc") String order) {
        return PaginationSupport.response(service.list(PaginationSupport.request(page, size, order)));
    }
    @GetMapping("/{youtubeId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Find episode by YouTube ID", description = "Case-sensitive eleven-character YouTube ID, not the database UUID.")
    public Episode get(@PathVariable String youtubeId) { return service.get(youtubeId); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Publish episode", description = "Fetches YouTube metadata; description may override provider text.")
    public ResponseEntity<Episode> create(@Valid @RequestBody CreateEpisodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete episode", description = "The path id is the internal UUID, not the YouTube ID.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
