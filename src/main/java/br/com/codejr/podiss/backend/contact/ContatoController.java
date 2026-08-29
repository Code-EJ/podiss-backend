package br.com.codejr.podiss.backend.contact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contatos")
public class ContatoController {

    @Autowired
    private ContatoService service;

    @PostMapping
    public ResponseEntity<Contato> createContato(@RequestBody Contato contato) {
        Contato novoContato = service.saveContato(contato);
        return ResponseEntity.ok(novoContato);
    }

    @GetMapping
    public ResponseEntity<List<Contato>> getAllContatos() {
        List<Contato> contatos = service.getAllContatos();
        return ResponseEntity.ok(contatos);
    }
}
