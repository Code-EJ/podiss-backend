// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import br.com.codejr.podiss.backend.common.PaginationSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
/**
 * Exposes public editorial reads and administrator-only writes. Creation and image replacement are multipart; text updates are JSON.
 *
 * @author oEnzoRibas
 */
@RestController @RequestMapping("/posts") @RequiredArgsConstructor
public class PostController {
    private final PostService service;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create post", description = "Multipart title, description, optional repeated tags and optional image; JPEG/PNG/GIF/WebP up to 5 MiB.")
    public ResponseEntity<PostResponse> create(@Valid @ModelAttribute CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List posts", description = "Returns summaries without image bytes; tags is a comma-separated response string.")
    public ResponseEntity<List<PostResponse>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size, @RequestParam(defaultValue = "asc") String order) {
        return PaginationSupport.response(service.list(PaginationSupport.request(page, size, order)));
    }
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Read post", description = "Returns summary and relative imageUrl when present.")
    public PostResponse get(@PathVariable UUID id) { return service.get(id); }
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE) @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update post text", description = "JSON PUT with partial semantics: omitted or null fields are preserved; empty tags clears tags.")
    public PostResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePostRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete post", description = "Removes post and stored image.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Replace post image", description = "Multipart part image; JPEG/PNG/GIF/WebP up to 5 MiB.")
    public PostResponse replaceImage(@PathVariable UUID id, @RequestPart("image") MultipartFile image) {
        return service.replaceImage(id, image);
    }
    @DeleteMapping("/{id}/image") @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Remove post image", description = "Keeps post text; returns no body.")
    public ResponseEntity<Void> removeImage(@PathVariable UUID id) {
        service.removeImage(id); return ResponseEntity.noContent().build();
    }
    @GetMapping("/image/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Read post image", description = "Binary data with detected image Content-Type; absent image returns 404.")
    public ResponseEntity<byte[]> image(@PathVariable UUID id) {
        var image = service.image(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
            .contentLength(image.bytes().length).body(image.bytes());
    }
}
