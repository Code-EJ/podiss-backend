# Application Runtime Configuration & Resources

Créditos: oEnzoRibas. Atualizado para a instalação limpa da etapa 04.

Diretório de recursos e configurações de execução da aplicação Spring Boot (`podiss-backend`).

---

## 🏗 Arquitetura de Profiles

A aplicação adota a convenção de profiles do Spring Boot (`application-{profile}.yaml`).

* **`application.yaml` (Base):** Contém todas as configurações globais, mapeamento de variáveis de ambiente, validações de payload (`multipart`), limites de taxa (`submissions`), definições do pool HikariCP e configurações comuns do JPA/Hibernate.
* **`application-dev.yaml` (Dev Local):** Ativado com `SPRING_PROFILES_ACTIVE=dev`.
    * Mantém `show-sql: false` no Hibernate.
    * Desativa migrations no startup e baseline automático, assim como a configuração base.
* **`application-prod.yaml` (Produção):** Ativado com `SPRING_PROFILES_ACTIVE=prod`.
    * Força a desativação de exibição de SQLs no console.
    * Fornece suporte opcional a SSL/TLS nativo via Keystore (PKCS12) caso a terminação TLS não ocorra em camada de Reverse Proxy.

---

## 🔑 Mapeamento de Variáveis de Ambiente (`.env`)

A aplicação falhará durante a inicialização caso variáveis obrigatórias não estejam presentes no ambiente.

| Variável | Descrição | Valor Fallback | Requerido em Prod |
| :--- | :--- | :--- | :--- |
| `DB_URL` | URL JDBC de conexão com o MariaDB | N/A | **Sim** |
| `DB_USERNAME` | Usuário de autenticação no banco | N/A | **Sim** |
| `DB_PASSWORD` | Senha de autenticação no banco | N/A | **Sim** |
| `JWT_SECRET` | Chave secreta HMAC-SHA512 (Base64, **Min. 64 bytes**) | N/A | **Sim** |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para requisições CORS (delimitadas por vírgula) | `http://localhost:5173` | **Sim** |
| `ADMIN_USERNAME` | Username para criação do Admin Bootstrap | `""` | Opcional |
| `ADMIN_EMAIL` | E-mail para criação do Admin Bootstrap | `""` | Opcional |
| `ADMIN_PASSWORD` | Senha para criação do Admin Bootstrap | `""` | Opcional |
| `YOUTUBE_API_KEY` | Chave de integração com a API do YouTube | `""` | Opcional |
| `HIKARI_MAX_LIFETIME` | Tempo máximo de vida útil de conexões no Pool (ms) | `1800000` (30m) | Opcional |

---

## 🗄 Migração de Banco de Dados (Flyway)

* A instalação nova usa exclusivamente `src/main/resources/db/fresh-migration/`.
* `db/migration/` e a V2 Java são legado preservado; não combinar as duas cadeias.
* Flyway está desativado no startup; aplicar a cadeia nova por executor externo antes de iniciar a aplicação com `ddl-auto=validate`.
* Convenção de nome: `V<VERSAO>__<DESCRICAO>.sql` (ex: `V1__create_users_table.sql`).
* Os defaults são `baseline-on-migrate=false` e `clean-disabled=true`. Propriedades externas podem sobrescrevê-los: conferir a configuração efetiva. Não fazer baseline em banco vazio.
* Veja o [relatório da etapa 04](../../../docs/ADRS/0002/relatorios/etapa04-instalacao-limpa.md) para testes, CLI e pendências de deploy. O plano Business Web Hosting não oferece suporte ao runtime Java.

---

## 🚀 Bootstrap Pattern (`app.bootstrap`)

Com as três credenciais `app.bootstrap.*` válidas, o bootstrap cria um ADMIN se ainda não existir nenhum. Não promove usuário existente; conflitos de username/email impedem o provisionamento.

> Em produção, forneça credenciais fortes fora do código. Após confirmar o provisionamento, remova as três variáveis ADMIN_USERNAME/ADMIN_EMAIL/ADMIN_PASSWORD juntas; deixar apenas parte delas pode impedir o startup.
