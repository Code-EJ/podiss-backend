package br.com.codejr.podiss.backend.sugest;// src/main/java/com/code/yolanda/back/sugestao/SugestaoRepository.java


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SugestaoRepository extends JpaRepository<Sugestao, UUID> {
}