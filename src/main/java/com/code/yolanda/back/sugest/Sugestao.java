package com.code.yolanda.back.sugest;// src/main/java/com/code/yolanda/back/sugestao/Sugestao.java


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "sugestoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sugestao {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String tema;
}
