# Application Runtime Configuration & Resources

Diretório de recursos e configurações de execução da aplicação Spring Boot (`podiss-backend`).

---

## 🏗 Arquitetura de Profiles

A aplicação adota a convenção de profiles do Spring Boot (`application-{profile}.yaml`).

* **`application.yaml` (Base):** Contém todas as configurações globais, mapeamento de variáveis de ambiente, validações de payload (`multipart`), limites de taxa (`submissions`), definições do pool HikariCP e configurações comuns do JPA/Hibernate.
* **`application-dev.yaml` (Dev Local):** Ativado com `SPRING_PROFILES_ACTIVE=dev`.
    * Habilita `show-sql: true` no Hibernate.
    * Habilita `spring.flyway.baseline-on-migrate: true` para facilitar a inicialização de bases locais de desenvolvimento.
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

* Os scripts DDL/DML de migração devem ser armazenados em `src/main/resources/db/migration/`.
* Convenção de nome: `V<VERSAO>__<DESCRICAO>.sql` (ex: `V1__create_users_table.sql`).
* Em ambiente de **Produção**, as propriedades `baseline-on-migrate` e `clean-disabled` estão travadas para prevenir manipulações destrutivas involuntárias na base de dados.

---

## 🚀 Bootstrap Pattern (`app.bootstrap`)

Se a tabela de usuários da base estiver vazia no momento da inicialização da aplicação, o serviço de bootstrap utilizará as credenciais definidas em `app.bootstrap.*` para instanciar a conta administrativa raiz.

> **Importante de Segurança:** Em ambiente produtivo, defina uma senha forte via variável de ambiente `ADMIN_PASSWORD` na primeira execução e remova a variável do ambiente após o provisionamento do admin.