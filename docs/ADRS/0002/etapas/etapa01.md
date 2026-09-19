Quero iniciar uma revisão estrutural completa deste backend Spring Boot, mas vamos trabalhar incrementalmente.

Existe um plano maior envolvendo padronização dos nomes para inglês, revisão do banco MariaDB, Flyway, segurança, DevOps, ADRs, Javadoc e OpenAPI. NÃO execute essas etapas ainda.

Nesta primeira etapa quero APENAS AUDITORIA E INVENTÁRIO.

IMPORTANTE: a aplicação já está rodando em produção na Hostinger e utiliza um banco MariaDB com dados reais.

NÃO modifique nenhum arquivo nesta etapa.
NÃO execute migrations.
NÃO altere o schema.
NÃO renomeie classes, métodos, tabelas, colunas ou endpoints.
NÃO atualize dependências.

Analise o projeto atual, incluindo:

* `pom.xml` e dependências Maven;
* `application.yaml` e profiles;
* `.env.example` e referências a variáveis de ambiente;
* configuração Spring Boot;
* configuração JPA/Hibernate;
* configuração Flyway e migrations existentes;
* entities e seus mapeamentos;
* repositories;
* services;
* controllers;
* DTOs;
* security/configurations;
* estrutura de packages;
* testes.

Quero que você primeiro entenda como o sistema funciona atualmente.

Durante a análise, faça também um inventário das nomenclaturas utilizadas no projeto e identifique:

* nomes em português;
* mistura de português e inglês;
* nomenclaturas inconsistentes;
* abreviações pouco claras;
* nomes que poderiam representar melhor o domínio.

Para cada ocorrência relevante, classifique uma eventual mudança como:

1. `JAVA_ONLY` — pode ser alterada sem afetar API ou banco;
2. `API_IMPACT` — pode alterar JSON, endpoint ou contrato externo;
3. `DATABASE_IMPACT` — pode alterar tabela, coluna, constraint, índice ou migration;
4. `CONFIGURATION_IMPACT` — pode afetar environment variables, YAML, deploy ou infraestrutura.

Analise também as dependências Maven procurando:

* dependências redundantes;
* dependências aparentemente não utilizadas;
* scopes inadequados;
* conflitos;
* versões potencialmente problemáticas;
* vulnerabilidades conhecidas que mereçam investigação;
* configurações de segurança relevantes.

Não faça alterações com base nessas descobertas ainda.

Para Flyway e banco de dados, identifique:

* migrations existentes;
* convenção utilizada;
* entities e seus respectivos `@Table`/`@Column`;
* possíveis dependências entre nomes Java e nomes físicos do banco;
* riscos de futuras renomeações;
* qualquer configuração que possa representar risco para produção.

Ao final, produza um relatório estruturado contendo:

1. visão geral da arquitetura atual;
2. inventário das configurações;
3. análise das dependências;
4. inventário de nomenclaturas;
5. mapa preliminar Java ↔ API ↔ banco;
6. análise do Flyway;
7. problemas de segurança encontrados;
8. problemas de DevOps/configuração encontrados;
9. riscos para produção;
10. dívida técnica observada;
11. mudanças que parecem seguras;
12. mudanças que exigem planejamento;
13. questões que precisam ser respondidas antes da próxima etapa.

Não implemente as recomendações.

O objetivo desta etapa é produzir uma baseline confiável do estado atual do projeto para utilizarmos nas próximas etapas.

Ao terminar, pare e aguarde revisão antes de modificar qualquer arquivo.
