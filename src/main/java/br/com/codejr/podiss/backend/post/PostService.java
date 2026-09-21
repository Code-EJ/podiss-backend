// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import br.com.codejr.podiss.backend.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
/**
 * Owns transactional editorial writes and image lifecycle. Read projections avoid loading blobs except for the image endpoint.
 *
 * @author oEnzoRibas
 */
@Service @RequiredArgsConstructor @Transactional
public class PostService {
    private final PostRepository repository;
    private final ImageValidator images;
    public PostResponse create(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle().trim());
        post.setDescription(request.getDescription().trim());
        post.setTags(tags(request.getTags()));
        post.setCreatedAt(Timestamp.from(Instant.now()));
        if (request.getImage() != null && !request.getImage().isEmpty()) assignImage(post, request.getImage());
        return PostResponse.from(repository.saveAndFlush(post));
    }
    @Transactional(readOnly = true)
    public Page<PostResponse> list(Pageable page) { return repository.findSummaries(page); }
    @Transactional(readOnly = true)
    public PostResponse get(UUID id) { return PostResponse.from(required(id)); }
    /**
     * Updates only supplied text fields; nulls preserve existing values and images are untouched.
     * @param id internal post UUID
     * @param request validated partial fields; an empty tags list clears tags
     * @return summary after persistence
     * @throws ApiException if absent or if tags contain commas or normalize to blanks
     */
    public PostResponse update(UUID id, UpdatePostRequest request) {
        Post post = required(id);
        if (request.title() != null) post.setTitle(request.title().trim());
        if (request.description() != null) post.setDescription(request.description().trim());
        if (request.tags() != null) post.setTags(tags(request.tags()));
        return PostResponse.from(repository.saveAndFlush(post));
    }
    public PostResponse replaceImage(UUID id, MultipartFile file) {
        Post post = required(id); assignImage(post, file);
        return PostResponse.from(repository.saveAndFlush(post));
    }
    public void removeImage(UUID id) {
        Post post = required(id); post.setImage(null); post.setImageContentType(null);
        repository.save(post);
    }
    @Transactional(readOnly = true)
    public ImageValidator.Image image(UUID id) {
        Post post = required(id);
        if (post.getImage() == null) throw new ApiException(HttpStatus.NOT_FOUND, "Post sem imagem.");
        return new ImageValidator.Image(post.getImage(), post.getImageContentType());
    }
    public void delete(UUID id) { repository.delete(required(id)); }
    private Post required(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post não encontrado."));
    }
    private void assignImage(Post post, MultipartFile file) {
        var image = images.read(file);
        post.setImage(image.bytes()); post.setImageContentType(image.contentType());
    }
    private String tags(List<String> values) {
        if (values == null) return "";
        // Accept legacy form values such as '"tag1","tag2"' without saving JSON quotes.
        return values.stream().map(String::trim).map(s -> s.replaceAll("^\\\"|\\\"$", ""))
            .peek(s -> { if (s.isBlank() || s.contains(","))
                throw new ApiException(HttpStatus.BAD_REQUEST, "Tags não podem ser vazias nem conter vírgula."); })
            .distinct().collect(java.util.stream.Collectors.joining(","));
    }
}
