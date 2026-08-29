// src/main/java/com/code/yolanda/back/post/PostController.java
package br.com.codejr.podiss.backend.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> createPost(@ModelAttribute PostRequest request) throws IOException {
        Post post;
        post = postService.createPost(request.getTitle(), request.getDescription(), request.getTags(), request.getImage().getBytes());

        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.findAllPosts();
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable UUID id) {
        Optional<Post> optionalPost = postService.findPostById(id);
        return optionalPost.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> updatePost(@PathVariable String id, @RequestBody PostRequest request) throws IOException {
        byte[] imageBytes = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageBytes = request.getImage().getBytes();
        }
        Post updatedPost = postService.updatePost(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getTags(),
                imageBytes
        );

        if (updatedPost != null) {
            return ResponseEntity.ok(updatedPost);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        Optional<Post> optionalPost = postService.findPostById(id);
        if (optionalPost.isPresent() && optionalPost.get().getImage() != null) {
            byte[] contents = optionalPost.get().getImage();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Ajuste conforme o tipo real da imagem.
            headers.setContentLength(contents.length);
            return new ResponseEntity<>(contents, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}