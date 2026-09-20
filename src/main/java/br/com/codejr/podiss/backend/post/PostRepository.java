// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.UUID;
public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("select new br.com.codejr.podiss.backend.post.PostResponse(p.id, p.title, p.description, p.tags, p.createdAt, " +
           "case when p.imageContentType is not null then true else false end) from Post p")
    Page<PostResponse> findSummaries(Pageable pageable);
}
