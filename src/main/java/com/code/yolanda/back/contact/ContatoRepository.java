

package com.code.yolanda.back.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ContatoRepository extends JpaRepository<Contato, UUID> {
}
