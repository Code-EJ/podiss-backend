package com.code.yolanda.back.contact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContatoService {
    @Autowired
    private ContatoRepository repository;

    public Contato saveContato(Contato contato) {
        return repository.save(contato);
    }

    public List<Contato> getAllContatos() {
        return repository.findAll();
    }
}
