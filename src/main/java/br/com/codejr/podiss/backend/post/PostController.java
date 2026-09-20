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
@RestController @RequestMapping("/posts") @RequiredArgsConstructor
public class PostController {
    private final PostService service;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> create(@Valid @ModelAttribute CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping
    public ResponseEntity<List<PostResponse>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size, @RequestParam(defaultValue = "asc") String order) {
        return PaginationSupport.response(service.list(PaginationSupport.request(page, size, order)));
    }
    @GetMapping("/{id}")
    public PostResponse get(@PathVariable UUID id) { return service.get(id); }
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE) @PreAuthorize("hasRole('ADMIN')")
    public PostResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePostRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('ADMIN')")
    public PostResponse replaceImage(@PathVariable UUID id, @RequestPart("image") MultipartFile image) {
        return service.replaceImage(id, image);
    }
    @DeleteMapping("/{id}/image") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeImage(@PathVariable UUID id) {
        service.removeImage(id); return ResponseEntity.noContent().build();
    }
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> image(@PathVariable UUID id) {
        var image = service.image(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
            .contentLength(image.bytes().length).body(image.bytes());
    }
}
