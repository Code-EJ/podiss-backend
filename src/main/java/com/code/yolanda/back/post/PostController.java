// src/main/java/com/code/yolanda/back/post/PostController.java
package com.code.yolanda.back.post;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post post = postService.createPost(request.getTitle(), request.getDescription(), request.getTags());
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
    public ResponseEntity<Post> updatePost(@PathVariable String id, @RequestBody PostRequest request) {
        Post updatedPost = postService.updatePost(id, request.getTitle(), request.getDescription(), request.getTags());
        if (updatedPost != null) {
            return ResponseEntity.ok(updatedPost);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

}

@Data
class PostRequest {
    private String title;
    private String description;
    private List<String> tags;
}
