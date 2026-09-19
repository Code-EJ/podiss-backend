Utilize o relatório produzido na Etapa 1 como baseline.

Nesta etapa, quero padronizar a nomenclatura técnica do projeto para INGLÊS, priorizando inicialmente mudanças internas e seguras.

O sistema está em produção. Portanto, preserve compatibilidade com:

* banco MariaDB existente;
* migrations Flyway já aplicadas;
* contratos REST existentes;
* payloads JSON consumidos por clientes;
* variáveis de ambiente utilizadas no deploy;
* configurações da Hostinger.

Antes de modificar qualquer nome, classifique a mudança como:

* `JAVA_ONLY`
* `API_IMPACT`
* `DATABASE_IMPACT`
* `CONFIGURATION_IMPACT`

Nesta etapa, implemente SOMENTE mudanças comprovadamente `JAVA_ONLY`.

Analise e padronize, quando necessário:

* packages;
* classes;
* interfaces;
* enums;
* métodos;
* parâmetros;
* variáveis locais;
* atributos internos;
* constantes;
* services;
* repositories;
* exceptions;
* utilities;
* nomes internos de DTOs quando isso não alterar serialização;
* nomenclaturas internas de configuração quando isso não alterar propriedades externas.

Use inglês técnico natural e consistente. Não faça traduções literais ruins.

Prefira nomes que expressem responsabilidade e domínio claramente.

Antes de renomear fields de entities ou DTOs, verifique cuidadosamente annotations como:

* `@Column`;
* `@Table`;
* `@JsonProperty`;
* `@JsonAlias`;
* `@Enumerated`;
* annotations de validação;
* annotations Jackson;
* annotations JPA.

Um atributo Java pode ser renomeado preservando explicitamente o nome físico existente por meio de `@Column`, quando apropriado.

Da mesma forma, não altere silenciosamente nomes serializados em JSON.

NÃO altere nesta etapa:

* tabelas;
* colunas;
* constraints;
* índices;
* migrations Flyway existentes;
* endpoints;
* paths;
* JSON público;
* environment variables utilizadas em produção;
* comportamento da aplicação;
* regras de negócio.

Depois das mudanças, execute os testes existentes e compile o projeto.

Verifique também referências quebradas, imports, reflection, JPQL, queries derivadas do Spring Data, SpEL, Jackson e qualquer mecanismo no qual um rename possa ter efeito indireto.

Ao final, apresente:

1. nomes alterados;
2. justificativa das alterações;
3. nomes deliberadamente preservados;
4. itens `API_IMPACT` deixados para depois;
5. itens `DATABASE_IMPACT` deixados para depois;
6. itens `CONFIGURATION_IMPACT` deixados para depois;
7. resultado de build e testes;
8. riscos ou dúvidas restantes.

Não avance para alterações de banco.

Pare ao finalizar esta etapa.
