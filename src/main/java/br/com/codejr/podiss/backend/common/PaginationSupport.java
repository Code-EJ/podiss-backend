// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.common;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.*;
/**
 * Preserves the legacy array response while exposing pagination metadata in headers. Stable ordering uses creation time and UUID.
 *
 * @author oEnzoRibas
 */
public final class PaginationSupport {
    private PaginationSupport() {}
    /**
     * Builds stable, bounded pagination rather than accepting arbitrary property sorting.
     * @param page zero-based page number
     * @param size number of records, between 1 and 100
     * @param order exactly {@code asc} or {@code desc}
     * @return request sorted by createdAt and id
     * @throws ApiException when any pagination argument is outside the accepted contract
     */
    public static Pageable request(int page, int size, String order) {
        if (page < 0 || size < 1 || size > 100 || !List.of("asc", "desc").contains(order))
            throw new ApiException(HttpStatus.BAD_REQUEST, "page >= 0; size entre 1 e 100; order asc ou desc.");
        Sort.Direction direction = Sort.Direction.fromString(order);
        return PageRequest.of(page, size, Sort.by(direction, "createdAt", "id"));
    }
    public static <T> ResponseEntity<List<T>> response(Page<T> page) {
        return ResponseEntity.ok().header("X-Total-Count", Long.toString(page.getTotalElements()))
            .header("X-Total-Pages", Integer.toString(page.getTotalPages()))
            .header("X-Page", Integer.toString(page.getNumber()))
            .header("X-Page-Size", Integer.toString(page.getSize())).body(page.getContent());
    }
}
