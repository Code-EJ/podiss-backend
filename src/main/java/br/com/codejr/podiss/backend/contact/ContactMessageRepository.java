

package br.com.codejr.podiss.backend.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * UUID-based contact persistence. The legacy bean alias is retained for compatibility with existing Spring references.
 *
 * @author oEnzoRibas
 */
@org.springframework.stereotype.Repository("contatoRepository")
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {
}
