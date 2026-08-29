package br.com.codejr.podiss.backend.post;
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

    public Post createPost(String title, String description, List<String> tags, byte[] image) {
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        post.setImage(image);
        
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

    public Post updatePost(String id, String title, String description, List<String> tags, byte[] imageBytes) {
        Optional<Post> optionalPost = repository.findById(UUID.fromString(id));
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            // Atualiza o título se não for nulo e não vazio
            if (title != null && !title.trim().isEmpty()) {
                post.setTitle(title);
            }

            // Atualiza a descrição se não for nula e não vazia
            if (description != null && !description.trim().isEmpty()) {
                post.setDescription(description);
            }

            // Atualiza as tags se não for nulo.
            // Se for lista vazia, significa remover as tags
            if (tags != null) {
                String tagsString = tags.isEmpty() ? "" : String.join(",", tags);
                post.setTags(tagsString);
            }

            // Atualiza a imagem apenas se imageBytes não for nulo
            if (imageBytes != null) {
                post.setImage(imageBytes);
            }

            return repository.save(post);
        }
        return null;
    }
}
