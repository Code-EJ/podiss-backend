// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.suggestion;
import br.com.codejr.podiss.backend.common.PaginationSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * Accepts public topic suggestions and restricts paginated reading to administrators.
 *
 * @author oEnzoRibas
 */
@RestController("sugestaoController") @RequestMapping("/sugestoes") @RequiredArgsConstructor
public class TopicSuggestionController {
    private final TopicSuggestionService service;
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Submit topic suggestion", description = "Accepts nome, email and tema; subject to per-address quota.")
    public ResponseEntity<TopicSuggestion> create(@Valid @RequestBody CreateTopicSuggestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "List topic suggestions", description = "Returns a paginated array containing submitter details.")
    public ResponseEntity<List<TopicSuggestion>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size, @RequestParam(defaultValue = "asc") String order) {
        return PaginationSupport.response(service.list(PaginationSupport.request(page, size, order)));
    }
}
