package com.code.yolanda.back.sugest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SugestaoService {
    @Autowired
    private SugestaoRepository repository;

    public Sugestao saveSugestao(Sugestao sugestao) {
        return repository.save(sugestao);
    }

    public List<Sugestao> getAllSugestoes() {
        return repository.findAll();
    }
}
