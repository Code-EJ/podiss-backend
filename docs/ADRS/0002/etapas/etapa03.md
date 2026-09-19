Utilize os resultados das Etapas 1 e 2 como baseline.

Agora quero analisar especificamente a padronização do BANCO DE DADOS para inglês e definir uma estratégia segura de evolução do schema.

IMPORTANTE:

O MariaDB está em produção na Hostinger e contém dados reais.

NESTA ETAPA NÃO EXECUTE ALTERAÇÕES NO BANCO DE PRODUÇÃO.

NÃO altere migrations Flyway já existentes.

NÃO execute migrations destrutivas.

NÃO renomeie tabelas ou colunas automaticamente.

Primeiro construa um mapa completo entre:

Java/JPA → API → Flyway → MariaDB.

Para cada entity, identifique:

* classe Java;
* `@Table`;
* atributos;
* `@Column`;
* relacionamentos;
* PK;
* FK;
* unique constraints;
* índices conhecidos;
* enums;
* queries associadas;
* repositories;
* migrations que criaram/modificaram a estrutura.

Identifique tabelas, colunas, constraints e outros objetos em português ou com nomenclatura inconsistente.

Para cada candidato a rename, informe:

* nome atual;
* nome proposto;
* motivo;
* objetos dependentes;
* impacto sobre JPA;
* impacto sobre queries;
* impacto sobre Flyway;
* impacto sobre dados;
* impacto sobre versões antigas da aplicação;
* risco operacional;
* necessidade real da mudança.

Questione explicitamente se cada rename físico vale o risco.

Considere três alternativas:

A. manter o nome físico legado e utilizar nomes Java em inglês por meio de mapeamento JPA explícito;

B. migrar imediatamente o nome físico;

C. fazer migração gradual utilizando estratégia expand-and-contract.

Não escolha automaticamente B apenas por consistência estética.

Para mudanças que realmente justifiquem migration física, desenhe a estratégia de migration.

Considere:

* backward compatibility;
* versões antiga e nova coexistindo durante deploy;
* locks;
* constraints;
* índices;
* foreign keys;
* volume de dados;
* backup;
* restore;
* downtime;
* possibilidade de deploy parcial;
* falha durante migration;
* rollback da aplicação;
* forward-fix do banco.

Não trate "rollback" de banco automaticamente como executar SQL inverso.

Analise quando o caminho correto seria:

* rollback apenas da aplicação;
* manter schema compatível;
* restaurar backup;
* executar nova migration de correção;
* expand-and-contract.

Analise também as configurações atuais:

* `spring.jpa.hibernate.ddl-auto`;
* Flyway;
* `baseline-on-migrate`;
* `baseline-version`;
* `clean-disabled`;
* schema history.

Determine se são apropriadas para desenvolvimento e produção.

Produza um plano de migração dividido por risco:

* SAFE;
* LOW RISK;
* MEDIUM RISK;
* HIGH RISK;
* NOT WORTH MIGRATING.

Não execute as migrations propostas.

Ao final, produza:

1. mapa Java ↔ banco;
2. inconsistências encontradas;
3. renames candidatos;
4. renames que devem permanecer apenas no Java;
5. renames físicos justificáveis;
6. estratégia de migration para cada caso;
7. estratégia de backup;
8. estratégia de rollback/forward-fix;
9. testes necessários;
10. checklist antes da primeira migration.

Pare e aguarde aprovação antes de criar ou executar migrations.
