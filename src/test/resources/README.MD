# Test Environment Resources & Configurations

Diretório de configurações isoladas para a suíte de testes da aplicação (`src/test`).

---

## ⚙️ `application.yaml` (Test Profile)

Este arquivo de configuração é carregado automaticamente pelo Spring durante a execução do comando `mvn test` ou via JUnit `@SpringBootTest`.

### 🛡️ Isolamento e Determinismo
* **Versionado no Git:** Este arquivo deve permanecer no repositório para garantir que pipelines de CI/CD (ex: GitHub Actions) e a execução local de qualquer desenvolvedor ocorram sem dependência de variáveis de ambiente externas.
* **Segredos Mapeados:** Utiliza exclusivamente chaves genéricas de teste (mockadas), garantindo que nenhum dado real de infraestrutura seja exposto.

---

## 📂 Por que H2 e Flyway Desativado nos Testes do Contexto?

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:podiss-test;MODE=MariaDB;DB_CLOSE_DELAY=-1
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop
```
* **Desempenho e Leveza:** Os testes de integração contextuais sobem uma instância em memória do H2 (`jdbc:h2:mem`), descartando o banco ao final do ciclo de testes sem depender de um container Docker ativo.
* **Divergência de DDL entre H2 e MariaDB:** As migrations do Flyway em `src/main/resources/db/migration` utilizam sintaxe DDL específica do MariaDB (`ENGINE=InnoDB`, cláusulas de índices locais, collation UTF8MB4). Para evitar falhas de parse do H2 na execução dessas migrations, o Flyway é desativado nesses testes e o esquema é gerado diretamente pelo Hibernate via `ddl-auto: create-drop`.

---

## 🐳 Testando Migrations Nativas do Flyway (Testcontainers)

Para testar a integridade das migrations SQL reais contra um motor MariaDB idêntico ao de produção, utilize a classe de teste anotada com Testcontainers:
```java
@Testcontainers
@SpringBootTest
class FlywayMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:11.2");

    // As migrations reais do Flyway serão executadas contra o container Docker
    }
```