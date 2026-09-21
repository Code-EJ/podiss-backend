// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.contact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.sql.Timestamp;
import java.time.Instant;
/**
 * Trims validated contact input and timestamps it at insertion. Transactional creation flushes constraints before returning.
 *
 * @author oEnzoRibas
 */
@Service("contatoService") @RequiredArgsConstructor
public class ContactMessageService {
    private final ContactMessageRepository repository;
    @Transactional
    public ContactMessage create(CreateContactMessageRequest request) {
        ContactMessage entity = new ContactMessage();
        entity.setSenderName(request.nome().trim());
        entity.setEmail(request.email().trim());
        entity.setSubject(request.assunto().trim());
        entity.setMessage(request.mensagem().trim());
        entity.setCreatedAt(Timestamp.from(Instant.now()));
        return repository.saveAndFlush(entity);
    }
    public Page<ContactMessage> list(Pageable pageable) { return repository.findAll(pageable); }
}
