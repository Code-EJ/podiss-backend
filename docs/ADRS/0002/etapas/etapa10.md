Faça uma auditoria final de tudo que foi produzido nas etapas anteriores.

O objetivo agora NÃO é adicionar novas funcionalidades.

Quero garantir que código, configuração e documentação contem a mesma história.

Revise:

* `README.md`;
* `docs/`;
* ADRs;
* deployment runbook;
* documentação de configuração;
* `.env.example`;
* `application*.yaml`;
* Javadocs;
* OpenAPI;
* Flyway;
* `pom.xml`;
* estrutura atual do projeto.

Verifique inconsistências entre documentação e implementação.

Procure especialmente:

* nomes antigos em português que deveriam ter sido atualizados;
* documentação apontando para classes que não existem mais;
* environment variables não documentadas;
* environment variables documentadas mas não utilizadas;
* endpoints ausentes no OpenAPI;
* configurações divergentes entre README e YAML;
* ADRs incompatíveis com a implementação;
* instruções de deploy incompletas;
* migrations não mencionadas;
* secrets presentes indevidamente em documentação;
* exemplos inseguros;
* links internos quebrados;
* TODOs esquecidos.

Organize a documentação de forma profissional e navegável.

O `README.md` principal deve funcionar como porta de entrada, e não como depósito de toda a documentação.

Ele deve direcionar para documentação detalhada em `docs/`.

Avalie uma estrutura como:

docs/
├── adr/
├── architecture/
├── configuration/
├── deployment/
├── security/
└── api/

Use somente diretórios que tenham conteúdo real.

Execute a validação final:

* build;
* testes unitários;
* testes de integração disponíveis;
* validação Flyway;
* inicialização da aplicação em ambiente seguro;
* geração OpenAPI;
* geração de Javadoc, se configurada.

Não execute deploy em produção.

Ao final produza um relatório final contendo:

1. estado atual do projeto;
2. alterações realizadas ao longo das etapas;
3. estado da nomenclatura;
4. estado do banco e migrations;
5. estado de segurança;
6. estado da documentação;
7. cobertura OpenAPI;
8. cobertura Javadoc;
9. riscos residuais;
10. dívida técnica restante;
11. decisões ainda pendentes;
12. próximos passos recomendados.

Diferencie claramente:

* DONE;
* PARTIALLY DONE;
* PENDING DECISION;
* FUTURE IMPROVEMENT.

Não implemente itens classificados como FUTURE IMPROVEMENT nesta etapa.

O objetivo é encerrar esta rodada com uma baseline estável, documentada, reproduzível e segura para futuras evoluções.
