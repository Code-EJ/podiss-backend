Utilize as conclusões das etapas anteriores para registrar formalmente as decisões arquiteturais do projeto.

Crie ou organize:

`docs/adr/`

Utilize uma convenção consistente, por exemplo:

`0001-<decision>.md`
`0002-<decision>.md`

Cada ADR deve conter, quando aplicável:

* Title;
* Status;
* Context;
* Problem;
* Decision;
* Alternatives Considered;
* Consequences;
* Security Considerations;
* Operational Considerations;
* Migration Considerations;
* References.

Não invente decisões que o projeto ainda não tomou.

Quando algo ainda estiver em discussão, registre como Proposed em vez de Accepted.

Identifique, a partir do projeto real, quais decisões merecem ADR.

No mínimo, avalie ADRs para:

* estratégia de migrations com Flyway;
* Hibernate/JPA schema validation;
* política de nomenclatura Java e banco;
* compatibilidade com schema legado;
* estratégia de configuração por Spring profiles;
* gerenciamento de secrets;
* arquitetura de autenticação/JWT, se relevante;
* estratégia de TLS/reverse proxy, dependendo da infraestrutura real;
* estratégia de deploy.

Crie um ADR específico sobre DATABASE SCHEMA EVOLUTION.

Documente:

* MariaDB como banco atual;
* Flyway como source of truth da evolução do schema;
* migrations imutáveis depois de aplicadas;
* `ddl-auto=validate` em produção;
* política para novas migrations;
* estratégia para renames;
* expand-and-contract;
* backward compatibility;
* backup;
* restore;
* rollback da aplicação;
* forward-fix;
* migrations destrutivas;
* validação antes de produção.

Crie também um ADR para DEPLOYMENT STRATEGY, caso as informações disponíveis sejam suficientes.

Não suponha detalhes da Hostinger que não estejam comprovados pelo projeto ou pela configuração existente.

Quando faltar informação operacional, marque explicitamente:

`TODO / INFORMATION REQUIRED`

O objetivo é registrar decisões reais, não fabricar documentação.

Ao final, liste:

1. ADRs criados;
2. ADRs Accepted;
3. ADRs Proposed;
4. decisões ainda pendentes;
5. informações que precisam ser confirmadas.
