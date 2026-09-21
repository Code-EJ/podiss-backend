# ADR 0006 — documentação HTTP gerada e restrita ao desenvolvimento

Status: **Accepted** para esta baseline local. Créditos: **oEnzoRibas**.

Não existia integração OpenAPI. Spring Boot 3.4.1 usa springdoc-openapi-starter-webmvc-ui 2.8.9, fixado explicitamente; a [matriz oficial](https://springdoc.org/v2/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot) admite 2.7.x–2.8.x para Boot 3.4.x. Isso não implica certificação de segurança de toda a árvore.

Spec e UI desligados no YAML base; profile dev habilita ambos. Sem profile dev, inclusive prod, ficam desligados. Não ativar dev na VPS. A cadeia nega as URLs de documentação quando api-docs.enabled é false, inclusive para ADMIN. Não publicar por override sem nova revisão de exposição.

Anotações Operation/Schema descrevem o contrato e um customizer centraliza Bearer, ADMIN, erros e headers. Não altera paths, DTOs de negócio ou códigos retornados. Endpoints públicos não herdam Bearer obrigatório. Tokens inválidos apresentados ainda podem gerar 401.

UI em /swagger-ui/index.html e spec em /v3/api-docs. O botão Authorize usa Bearer; nunca inserir token de produção no ambiente local. Testes conferem cobertura e acesso. Não se armazena spec manual duplicada; a fonte de verdade é a geração testada.
