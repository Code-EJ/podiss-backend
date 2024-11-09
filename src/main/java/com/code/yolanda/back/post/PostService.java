package com.code.yolanda.back.post;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID; // Esta linha deve estar presente
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    public Post createPost(String title, String description, List<String> tags) {
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        //caso tag vazia = ''
        String tagsString = tags != null && !tags.isEmpty() ? String.join(",", tags) : "";
        post.setTags(tagsString);

        return repository.save(post);
    }
    public void deletePost(UUID id) {
        repository.deleteById(id);
    }


    public List<Post> findAllPosts() {
        return repository.findAll();
    }
    public Optional<Post> findPostById(UUID id) {
        return repository.findById(id);
    }

    public Post updatePost(String id, String title, String description, List<String> tags) {
        Optional<Post> optionalPost = repository.findById(UUID.fromString(id));
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setTitle(title);
            post.setDescription(description);

            String tagsString = String.join(",", tags);
            post.setTags(tagsString);

            return repository.save(post);
        }
        return null;
    }


}
