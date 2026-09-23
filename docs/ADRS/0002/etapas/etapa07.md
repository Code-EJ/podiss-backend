Utilize os problemas de segurança e configuração identificados nas etapas anteriores.

Agora quero revisar e, quando seguro, corrigir configurações relacionadas a segurança, dependências e DevOps.

Analise novamente:

* `pom.xml`;
* dependências diretas;
* dependências transitivas relevantes;
* Spring Security;
* JWT;
* CORS;
* upload de arquivos;
* datasource;
* HikariCP;
* Flyway;
* JPA;
* TLS;
* profiles;
* `.env.example`;
* secrets;
* logs;
* tratamento de erros;
* actuator, se existir;
* OpenAPI/Swagger, se já existir.

Classifique cada descoberta por severidade:

* CRITICAL;
* HIGH;
* MEDIUM;
* LOW;
* INFORMATIONAL.

Não classifique algo como vulnerabilidade apenas por estar desatualizado.

Quando mencionar CVE ou vulnerabilidade conhecida, forneça evidência concreta da dependência/versão afetada.

Avalie especialmente:

* secrets com defaults inseguros;
* secrets commitados;
* JWT secret strength;
* duração de tokens;
* CORS excessivamente permissivo;
* exposição de stack traces;
* endpoints administrativos;
* configuração de SSL/TLS;
* credentials em logs;
* upload de arquivos;
* limites de request;
* SQL logging;
* Flyway clean;
* configurações diferentes entre dev/prod;
* dependency scopes.

Faça mudanças somente quando:

1. o problema estiver comprovado;
2. a correção não quebrar produção;
3. o impacto estiver entendido.

Mudanças potencialmente breaking devem ser apenas propostas.

Execute build e testes após alterações.

Documente as decisões relevantes em ADR quando apropriado.

Ao final apresente:

1. vulnerabilidades confirmadas;
2. riscos de configuração;
3. alterações realizadas;
4. alterações recomendadas mas não executadas;
5. breaking changes potenciais;
6. resultado dos testes;
7. riscos residuais.

