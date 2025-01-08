package com.code.yolanda.back.sugest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sugestoes")
@CrossOrigin(origins = "http://localhost:5173")
public class SugestaoController {

    @Autowired
    private SugestaoService service;

    @PostMapping
    public ResponseEntity<Sugestao> createSugestao(@RequestBody Sugestao sugestao) {
        Sugestao novaSugestao = service.saveSugestao(sugestao);
        return ResponseEntity.ok(novaSugestao);
    }

    @GetMapping
    public ResponseEntity<List<Sugestao>> getAllSugestoes() {
        List<Sugestao> sugestoes = service.getAllSugestoes();
        return ResponseEntity.ok(sugestoes);
    }
}
