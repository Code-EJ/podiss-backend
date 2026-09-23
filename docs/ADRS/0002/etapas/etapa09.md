Agora documente formalmente a API REST utilizando OpenAPI/Swagger.

Primeiro determine qual solução OpenAPI já existe no projeto.

Se nenhuma existir, analise a versão atual do Spring Boot e proponha a integração apropriada antes de adicionar dependências.

Não altere comportamento dos endpoints.

Não altere paths.

Não altere payloads.

Não altere códigos HTTP existentes silenciosamente.

Documente os controllers e DTOs existentes.

Para cada endpoint relevante, documente:

* summary;
* description;
* operation;
* path;
* HTTP method;
* parâmetros;
* request body;
* response body;
* status codes;
* autenticação;
* autorização;
* erros esperados;
* schemas utilizados.

Utilize annotations OpenAPI adequadamente, sem poluir excessivamente controllers.

Avalie quando faz sentido centralizar documentação de:

* security schemes;
* JWT bearer authentication;
* API metadata;
* error responses;
* common schemas.

Documente DTOs com schemas quando isso agregar informação útil.

Não duplique Javadoc mecanicamente.

Considere a separação:

Javadoc:
documenta responsabilidade e comportamento do código.

OpenAPI:
documenta o contrato HTTP observado pelos consumidores.

Verifique se endpoints protegidos aparecem corretamente como protegidos na especificação.

Analise também se Swagger UI deve estar:

* habilitado em desenvolvimento;
* habilitado ou desabilitado em produção;
* protegido em produção.

Não tome essa decisão silenciosamente. Documente a decisão e, se arquiteturalmente relevante, registre-a em ADR.

Depois valide:

* aplicação inicia;
* especificação OpenAPI é gerada;
* Swagger UI funciona onde esperado;
* endpoints aparecem corretamente;
* schemas são válidos;
* configuração de security scheme funciona.

Execute build e testes.

Ao final apresente:

1. endpoints documentados;
2. schemas documentados;
3. configuração OpenAPI;
4. configuração Swagger UI;
5. decisões de segurança;
6. lacunas restantes;
7. resultado dos testes.
