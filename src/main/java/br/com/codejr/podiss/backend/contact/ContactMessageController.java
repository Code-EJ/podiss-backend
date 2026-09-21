// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.contact;
import br.com.codejr.podiss.backend.common.PaginationSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * Accepts public contact submissions and exposes administrator-only paginated reading. Validation occurs before persistence.
 *
 * @author oEnzoRibas
 */
@RestController("contatoController") @RequestMapping("/contatos") @RequiredArgsConstructor
public class ContactMessageController {
    private final ContactMessageService service;
    @PostMapping
    public ResponseEntity<ContactMessage> create(@Valid @RequestBody CreateContactMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessage>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size, @RequestParam(defaultValue = "asc") String order) {
        return PaginationSupport.response(service.list(PaginationSupport.request(page, size, order)));
    }
}
