

package br.com.codejr.podiss.backend.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

@org.springframework.stereotype.Repository("contatoRepository")
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {
}
